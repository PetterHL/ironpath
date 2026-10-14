// Original synthetic package scenarios, executed only in a separate pinned GPL comparator.
// No upstream implementation, equipment dataset or source text is included in IronPath.
import { test, expect } from '@jest/globals';
import { getTestPlayer, findEquipmentById, calculatePlayerVsNpc } from '@/tests/utils/TestUtils';
import { CUSTOM_MONSTER_BASE } from '@/lib/Monsters';
import { Prayer } from '@/enums/Prayer';
import { readFileSync, writeFileSync } from 'fs';

const catalog=JSON.parse(readFileSync(process.env.IRONPATH_CATALOG!, 'utf8'));
const weapons=[26219,22325,22486,28338,22978,29589,19675,11889,22324,23995,24417,29796,13263,13265,28810,4587,4151];
const packages=[
  {head:10828,body:10551,legs:11834,hands:7462,cape:6570,neck:6585,feet:11840,ring:11773},
  {head:26382,body:26384,legs:26386,hands:22981,cape:21295,neck:29801,feet:31091,ring:28307},
  {head:24419,body:24420,legs:24421,hands:22981,cape:21295,neck:19553,feet:13239,ring:28316},
  {head:11865,body:30753,legs:30756,hands:7462,cape:6570,neck:12018,feet:31088,ring:11773},
];
const targetIds=['neutral-zero-defence','dharok','graardor','vorkath-melee','duke-melee','olm-melee-scenario','akkha-melee-scenario','baba-melee-scenario'];
test('original complete melee family scenarios',()=>{
  const rows=['case,attack,strength,weapon,head,body,legs,hands,cape,neck,feet,ring,shield,prayer,boost,style,target,task,souls,defence,max,attackRoll,defenceRoll,chance,dps'];
  let count=0;
  for(const weapon of weapons) {
    const item=catalog.melee.items.find((i:any)=>i.id===weapon);
    for(const style of item.styles) for(const armour of packages) for(const prayer of ['NONE','PIETY']) for(const boosted of [false,true]) {
      for(const targetId of targetIds) for(const task of targetId==='vorkath-melee'||targetId==='duke-melee'?[false,true]:[false]) {
        for(const soul of weapon===28338?[0,1,2,3,4,5]:[0]) for(const defence of targetId==='olm-melee-scenario'?[-1,0,80]:[-1]) {
          const t=catalog.comparison.targets.find((x:any)=>x.id===targetId);
          const monster=JSON.parse(JSON.stringify(CUSTOM_MONSTER_BASE));
          monster.name=t.demonbanePercent===70?'Duke Sucellus':'Original IronPath fixed-input scenario';
          monster.id=t.variant==='TOA'?t.npcId:-1; monster.size=t.size; monster.weakness=null;
          monster.attributes=[...(t.dragon?['dragon']:[]),...(t.undead?['undead']:[]),...(t.demonbanePercent!=null?['demon']:[])];
          monster.skills.def=defence<0?t.defenceLevel:defence;
          monster.defensive.stab=t.meleeStab;monster.defensive.slash=t.meleeSlash;monster.defensive.crush=t.meleeCrush;
          const ids:any={...armour,weapon}; if(!item.twoHanded)ids.shield=12954;
          const equipment:any={}; Object.entries(ids).forEach(([slot,id])=>equipment[slot]=findEquipmentById(id as number));
          const [type,stance]=style.toLowerCase().split('_');
          const player=getTestPlayer(monster,{equipment,skills:{atk:90,str:90,def:99,prayer:99},
            style:{type:type as any,stance:(stance[0].toUpperCase()+stance.slice(1)) as any,name:style},
            prayers:prayer==='PIETY'?[Prayer.PIETY]:[],boosts:{atk:boosted?18:0,str:boosted?18:0},
            buffs:{onSlayerTask:task,soulreaperStacks:soul}});
          const result=calculatePlayerVsNpc(monster,player,{disableMonsterScaling:true});
          rows.push([++count,90,90,weapon,...['head','body','legs','hands','cape','neck','feet','ring','shield'].map(s=>ids[s]||0),
            prayer,boosted?'SUPER_ATTACK_STRENGTH':'NONE',style,targetId,task,soul,defence,result.maxHit,result.maxAttackRoll,result.npcDefRoll,result.accuracy,result.dps].join(','));
        }
      }
    }
  }
  expect(count).toBeGreaterThan(10000);
  writeFileSync('/tmp/r3b-family-oracle.csv',rows.join('\n')+'\n');
});
test('independently reviewed exact-form offensive facts',()=>{
  for(const item of catalog.melee.items) {
    const reference=findEquipmentById(item.id);
    expect([reference.offensive.stab,reference.offensive.slash,reference.offensive.crush,reference.bonuses.str])
      .toEqual([item.stab,item.slash,item.crush,item.strength]);
    if(item.slot==='weapon') {expect(reference.speed).toBe(item.ticks);expect(reference.isTwoHanded).toBe(item.twoHanded);}
  }
});
