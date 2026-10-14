// Original synthetic inputs. Execute only in the separate pinned GPL calculator checkout.
// No upstream implementation, equipment dataset or test corpus is included in IronPath.
import { test, expect } from '@jest/globals';
import { getTestPlayer, findEquipmentById, calculatePlayerVsNpc, getTestMonsterById } from '@/tests/utils/TestUtils';
import { CUSTOM_MONSTER_BASE } from '@/lib/Monsters';
import { Prayer } from '@/enums/Prayer';
import { writeFileSync, readFileSync } from 'fs';

const weapons: [number,string[]][] = [
  [4151,['SLASH_ACCURATE','SLASH_CONTROLLED','SLASH_DEFENSIVE']],
  [12006,['SLASH_ACCURATE','SLASH_CONTROLLED','SLASH_DEFENSIVE']],
  [21009,['STAB_ACCURATE','STAB_AGGRESSIVE','SLASH_AGGRESSIVE','STAB_DEFENSIVE']],
  [23528,['CRUSH_ACCURATE','CRUSH_AGGRESSIVE','STAB_CONTROLLED','CRUSH_DEFENSIVE']],
  [7158,['SLASH_ACCURATE','SLASH_AGGRESSIVE','CRUSH_AGGRESSIVE','SLASH_DEFENSIVE']],
  [1333,['SLASH_ACCURATE','SLASH_AGGRESSIVE','STAB_CONTROLLED','SLASH_DEFENSIVE']],
];
test('IronPath original ordinary melee package matrix', () => {
  const rows=['case,attack,strength,weapon,head,body,legs,hands,cape,neck,feet,ring,shield,prayer,boost,style,target,max,attackRoll,defenceRoll,chance,dps'];
  let count=0;
  for (const [attack,strength] of [[75,85],[90,90],[99,99]]) for (const [weapon,styles] of weapons) {
    for (const style of styles) for (const armour of [0,1]) for (const prayer of ['NONE','INCREDIBLE_ULTIMATE','CHIVALRY','PIETY']) {
      for (const boost of [false,true]) for (const target of ['neutral-zero-defence','graardor','dharok']) {
        const ids:any = armour===0
          ? {weapon,head:10828,body:10551,legs:11834,hands:7462,cape:6570,neck:6585,feet:11840,ring:11773}
          : {weapon,head:24271,body:11832,legs:11834,hands:22981,cape:21295,neck:29801,feet:13239,ring:22975};
        if (weapon!==7158) ids.shield=armour===0?12954:22322;
        const equipment:any={}; Object.entries(ids).forEach(([slot,id])=>equipment[slot]=findEquipmentById(id as number));
        const monster=JSON.parse(JSON.stringify(CUSTOM_MONSTER_BASE)); monster.attributes=[]; monster.weakness=null;
        monster.name='IronPath ordinary melee synthetic target';
        if (target==='graardor') {monster.skills.def=250;monster.defensive.stab=90;monster.defensive.slash=90;monster.defensive.crush=90;}
        if (target==='dharok') {monster.skills.def=100;monster.defensive.stab=252;monster.defensive.slash=250;monster.defensive.crush=244;}
        const [type,stance]=style.toLowerCase().split('_'); const stanceLabel=stance[0].toUpperCase()+stance.slice(1);
        const prayers=prayer==='NONE'?[]:prayer==='PIETY'?[Prayer.PIETY]:prayer==='CHIVALRY'?[Prayer.CHIVALRY]:[Prayer.INCREDIBLE_REFLEXES,Prayer.ULTIMATE_STRENGTH];
        const player=getTestPlayer(monster,{equipment,skills:{atk:attack,str:strength,def:99,prayer:99},
          style:{type:type as any,stance:stanceLabel as any,name:style},prayers,
          boosts:{atk:boost?5+Math.floor(attack*0.15):0,str:boost?5+Math.floor(strength*0.15):0}});
        const result=calculatePlayerVsNpc(monster,player);
        rows.push([++count,attack,strength,weapon,...['head','body','legs','hands','cape','neck','feet','ring','shield'].map(s=>ids[s]||0),
          prayer,boost?'SUPER_ATTACK_STRENGTH':'NONE',style,target,result.maxHit,result.maxAttackRoll,result.npcDefRoll,result.accuracy,result.dps].join(','));
      }
    }
  }
  expect(count).toBe(3168);
  writeFileSync('/tmp/r3b-oracle.csv',rows.join('\n')+'\n');
});

// Development-time cross-check only. Wiki facts were reviewed independently before this comparison.
test('reviewed ordinary facts agree with the pinned comparator data', () => {
  const catalog=JSON.parse(readFileSync(process.env.IRONPATH_CATALOG!, 'utf8'));
  for (const item of catalog.melee.items) {
    const reference=findEquipmentById(item.id);
    expect([reference.offensive.stab,reference.offensive.slash,reference.offensive.crush,reference.bonuses.str])
      .toEqual([item.stab,item.slash,item.crush,item.strength]);
    if (item.slot==='weapon') {expect(reference.speed).toBe(item.ticks);expect(reference.isTwoHanded).toBe(item.twoHanded);}
  }
  for (const id of ['dharok','graardor']) {
    const target=catalog.comparison.targets.find((t:any)=>t.id===id);
    const reference=getTestMonsterById(target.npcId);
    expect([reference.skills.def,reference.defensive.stab,reference.defensive.slash,reference.defensive.crush])
      .toEqual([target.defenceLevel,target.meleeStab,target.meleeSlash,target.meleeCrush]);
  }
});
