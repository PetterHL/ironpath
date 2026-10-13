// Original synthetic attack scenarios. Run only in a separate pinned GPL reference checkout.
import { test } from '@jest/globals';
import { getTestPlayer, findEquipmentById, calculatePlayerVsNpc } from '@/tests/utils/TestUtils';
import { CUSTOM_MONSTER_BASE } from '@/lib/Monsters';
import { Prayer } from '@/enums/Prayer';
import { MonsterAttribute } from '@/enums/MonsterAttribute';

test('IronPath first ranged family differential matrix', () => {
  const families = [
    {name:'CRYSTAL_BOW',id:23983,attack:100,strength:78},
    {name:'BOWFA',id:25867,attack:128,strength:106},
    {name:'TWISTED_BOW',id:20997,attack:70,strength:20},
  ];
  const pieces = [[],[23971],[23975,23979],[23971,23975,23979]];
  const profiles = [
    {def:0,bonus:0,magic:0,magicAttack:0,chambers:false},
    {def:150,bonus:75,magic:100,magicAttack:200,chambers:false},
    {def:250,bonus:200,magic:500,magicAttack:0,chambers:false},
    {def:250,bonus:200,magic:500,magicAttack:0,chambers:true},
  ];
  for (const ranged of [85,99]) for (const family of families) for (const style of ['Accurate','Rapid','Longrange'])
  for (const prayer of ['NONE','EAGLE_EYE','DEADEYE','RIGOUR']) for (let profile=0;profile<profiles.length;profile++)
  for (let crystal=0;crystal<pieces.length;crystal++) {
    const target=profiles[profile];
    const monster=JSON.parse(JSON.stringify(CUSTOM_MONSTER_BASE));
    // Only the tbow cap changes; synthetic final stats are supplied and scaling is disabled.
    monster.attributes=target.chambers?[MonsterAttribute.XERICIAN]:[];
    monster.skills.def=target.def;monster.skills.magic=target.magic;monster.offensive.magic=target.magicAttack;
    monster.defensive.standard=target.bonus;
    const equipment:any={weapon:findEquipmentById(family.id)};
    if(family.name==='TWISTED_BOW')equipment.ammo=findEquipmentById(11212);
    for(const id of pieces[crystal]) {
      const slot=id===23971?'head':id===23975?'body':'legs';equipment[slot]=findEquipmentById(id);
    }
    const ammoStrength=family.name==='TWISTED_BOW'?60:0;
    const player=getTestPlayer(monster,{skills:{ranged,prayer:99,def:99},equipment,
      style:{type:'ranged',stance:style as any,name:style},prayers:prayer==='NONE'?[]:[(Prayer as any)[prayer]],
      offensive:{stab:0,slash:0,crush:0,magic:0,ranged:family.attack+50},
      bonuses:{str:0,ranged_str:family.strength+10+ammoStrength,magic_str:0}});
    const r=calculatePlayerVsNpc(monster,player,{disableMonsterScaling:true});
    console.log('RANGED '+JSON.stringify({ranged,family:family.name,style,prayer,profile,crystal,
      max:r.maxHit,attack:r.maxAttackRoll,defence:r.npcDefRoll,accuracy:r.accuracy,dps:r.dps}));
  }
});
