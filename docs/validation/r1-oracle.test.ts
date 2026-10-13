// Original synthetic full-loadout inputs. Run only in a separate pinned GPL reference checkout.
import { test } from '@jest/globals';
import { getTestMonsterById, getTestPlayer, findEquipmentById, calculatePlayerVsNpc } from '@/tests/utils/TestUtils';
import { Prayer } from '@/enums/Prayer';

test('IronPath R1 powered-magic full-loadout matrix', () => {
  const packages = [[4708,4712,4714],[26241,26243,26245],[21018,21021,21024]];
  for (const magic of [85,90,99]) for (const weapon of [11907,12899,22323,31113,27275]) {
    for (let armour=0;armour<packages.length;armour++) for (const npc of [1673,2215]) {
      for (const style of ['Accurate','Longrange']) for (const scenario of ['none','might-potion','augury-heart']) {
        const ids: any = {head:packages[armour][0],body:packages[armour][1],legs:packages[armour][2],cape:21795,
          neck:12002,weapon,hands:19544,feet:13235,ring:11770};
        if (weapon !== 27275) ids.shield=25985;
        const equipment: any = {};
        Object.entries(ids).forEach(([slot,id])=>equipment[slot]=findEquipmentById(id as number));
        const monster=getTestMonsterById(npc);
        const player=getTestPlayer(monster,{skills:{magic,prayer:90,def:90},equipment,
          style:{type:'magic',stance:style as any,name:style},
          prayers:scenario==='augury-heart'?[Prayer.AUGURY]:scenario==='might-potion'?[Prayer.MYSTIC_MIGHT]:[],
          boosts:{magic:scenario==='might-potion'?4:scenario==='augury-heart'?4+Math.floor(magic/10):0}});
        const r=calculatePlayerVsNpc(monster,player);
        console.log('R1 '+JSON.stringify({magic,weapon,armour,npc,style,scenario,max:r.maxHit,attack:r.maxAttackRoll,defence:r.npcDefRoll,accuracy:r.accuracy,dps:r.dps}));
      }
    }
  }
});
