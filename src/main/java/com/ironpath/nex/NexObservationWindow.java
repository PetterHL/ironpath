package com.ironpath.nex;

import java.util.*;
import java.util.function.Predicate;

/** Worker-built allowlisted Diagnostics pages. Clipboard evidence is exported independently. */
public final class NexObservationWindow
{
	public static final int PAGE_SIZE=12,MAX_LINKS=8;
	private static final class Row
	{
		final int tick,cycle; final long order; final String target,detail; final NexTargetState.Form form;
		Row(int tick,int cycle,long order,String target,NexTargetState.Form form,String detail)
		{ this.tick=tick; this.cycle=cycle; this.order=order; this.target=target; this.form=form; this.detail=detail; }
	}
	private final List<String> lines;
	private final boolean truncated,limited;
	private int linkWork;
	private boolean linksLimited;
	public NexObservationWindow(NexSummary summary,NexObservationReview review)
	{
		var o=summary.observations; List<Row> rows=new ArrayList<>();
		for(int i=0;i<o.launches.size();i++)
		{
			var l=o.launches.get(i);
			rows.add(new Row(l.sample.tick,l.cycle,l.order,l.sample.target,l.sample.targetState.form,
				"Launch L"+(i+1)+"; weapon="+l.weapon+"; kind="+l.attack+"; animation="+l.sample.animation
				+"; reconciled="+review.reconciled.get(l)+"; "+l.sample.targetState.health.reading()));
		}
		for(var s:o.signals)
		{
			rows.add(new Row(s.tick,s.cycle,s.order,s.target,NexTargetState.Form.UNKNOWN,
				"Resource; kind="+s.kind+"; event/polled value="+s.value+"; server value="+s.serverValue
				+(s.state==null?"":"; settled energy="+s.state.specialEnergy+"; DC active/cooldown/upgrade="
					+s.state.resources.get("death-charge-active")+"/"+s.state.resources.get("death-charge-cooldown")+"/"+s.state.resources.get("death-charge-upgrade-raw"))));
		}
		for(int i=0;i<o.flights.size();i++)
		{
			var f=o.flights.get(i);
			rows.add(new Row(f.tick,f.cycle,f.order,f.target,f.targetState.form,
				"Projectile F"+(i+1)+"; id="+f.id+"; source="+f.source+"; start/end cycle offsets from this callback="+((long)f.startCycle-f.cycle)+"/"+((long)f.endCycle-f.cycle)
				+"; launch candidates="+links(o.launches,"L",l->f.source==NexObservations.Source.LOCAL && f.target!=null && Objects.equals(f.target,l.sample.target)
					&& (l.weapon==NexObservations.Weapon.TWISTED_BOW || l.weapon==NexObservations.Weapon.ZARYTE_CROSSBOW)
					&& l.order<f.order && Math.abs((long)f.startCycle-l.cycle)<=30)));
		}
		for(int i=0;i<o.impacts.size();i++)
		{
			var h=o.impacts.get(i); Set<NexObservations.Launch> candidates=new HashSet<>(review.matches.get(i).candidates);
			rows.add(new Row(h.tick,h.cycle,h.order,h.target,h.targetState.form,
				"Impact I"+(i+1)+"; amount="+h.amount+"; launch candidates="+links(o.launches,"L",candidates::contains)
				+"; projectile candidates="+links(o.flights,"F",f->f.source==NexObservations.Source.LOCAL && h.target!=null && Objects.equals(h.target,f.target)
					&& f.order<h.order && Math.abs((long)f.endCycle-h.cycle)<=30)+"; "+h.targetState.health.reading()));
		}
		for(var h:o.health)
		{ rows.add(new Row(h.tick,h.cycle,h.order,h.target,h.targetState.form,"Health; ratio/scale="+h.targetState.healthRatio+"/"+h.targetState.healthScale+"; "+h.targetState.health.reading())); }
		rows.sort(Comparator.comparingLong(r->r.order));
		int baseCycle=rows.isEmpty()?0:rows.get(0).cycle; long baseOrder=rows.isEmpty()?0:rows.get(0).order;
		Map<String,Integer> targets=new LinkedHashMap<>(); List<String> out=new ArrayList<>();
		for(var row:rows)
		{
			String target=row.target==null?"UNKNOWN":"T"+targets.computeIfAbsent(row.target,k->targets.size()+1)+"/"+targetClass(row.target);
			out.add("tick="+(row.tick-summary.startTick)+"; relative cycle="+(row.cycle-baseCycle)+"; relative order="+(row.order-baseOrder)
				+"; target="+target+"; form="+row.form+"; "+row.detail);
		}
		lines=List.copyOf(out); truncated=o.truncated; limited=review.correlationLimited || linksLimited;
	}
	private <T> String links(List<T> values,String prefix,Predicate<T> matches)
	{
		List<String> out=new ArrayList<>(); int count=0;
		for(int i=0;i<values.size();i++)
		{
			if(++linkWork>1000000) { linksLimited=true; return out+"; link search incomplete"; }
			if(matches.test(values.get(i))) { if(count<MAX_LINKS) { out.add(prefix+(i+1)); } count++; }
		}
		return out+(count>MAX_LINKS?"; links omitted="+(count-MAX_LINKS):"");
	}
	private static String targetClass(String target)
	{
		String value=target.split("@",2)[0];
		return Set.of("NEX","FUMUS","UMBRA","CRUOR","GLACIES").contains(value)?value:"UNKNOWN";
	}
	public int pages() { return Math.max(1,(lines.size()+PAGE_SIZE-1)/PAGE_SIZE); }
	public List<String> page(int requested)
	{
		int page=Math.max(0,Math.min(requested,pages()-1)),start=page*PAGE_SIZE,end=Math.min(start+PAGE_SIZE,lines.size());
		List<String> out=new ArrayList<>();
		out.add("Observation event window "+(page+1)+"/"+pages()+"; rows="+(lines.isEmpty()?0:start+1)+"–"+end+"/"+lines.size()
			+"; outside="+(lines.size()-(end-start))+"; truncated="+truncated+"; links incomplete="+limited);
		out.add("Client callback order. Local T/L/F/I refs; candidate links only.");
		out.addAll(lines.subList(start,end));
		return List.copyOf(out);
	}
}
