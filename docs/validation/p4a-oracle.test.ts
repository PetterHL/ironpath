// Original synthetic inputs for independent validation only. Run this harness in a
// separate GPL reference checkout; the Ironpath plugin never executes it.
import { test } from '@jest/globals';
import { getTestMonsterById, getTestPlayer, findEquipmentById, calculatePlayerVsNpc } from '@/tests/utils/TestUtils';
import { Prayer } from '@/enums/Prayer';

const m = getTestMonsterById(1673);
test('Ironpath P4a original synthetic trident reference matrix', () => {
  for (const magic of [75, 78, 89, 90, 92, 93, 99]) {
    for (const weapon of [11907, 12899]) {
      if (weapon === 12899 && magic < 78) continue;
      for (const neck of [0, 6585, 12002]) {
        for (const style of ['Accurate', 'Longrange']) {
          for (const scenario of ['none', 'might-potion', 'augury']) {
            const equipment: any = {weapon: findEquipmentById(weapon)};
            if (neck) equipment.neck = findEquipmentById(neck);
            const p = getTestPlayer(m, { skills: { magic, prayer: 90, def: 90 }, equipment,
              style: {type: 'magic', stance: style as any, name: style},
              prayers: scenario === 'augury' ? [Prayer.AUGURY] : scenario === 'might-potion' ? [Prayer.MYSTIC_MIGHT] : [],
              boosts: { magic: scenario === 'might-potion' ? 4 : 0 },
            });
            const r = calculatePlayerVsNpc(m,p);
            console.log('P4A ' + JSON.stringify({magic,weapon,neck,style,scenario,max:r.maxHit,attack:r.maxAttackRoll,defence:r.npcDefRoll,accuracy:r.accuracy,dps:r.dps}));
          }
        }
      }
    }
  }
});
