package com.telepathicgrunt.the_bumblezone.modcompat;

import com.telepathicgrunt.the_bumblezone.Bumblezone;

public class PotionOfBeesBeeSplashPotionProjectile
{
	/*
	 * Check for if potion of bee's item was thrown and impacted empty honeycomb block to revive it
	 */
	public static void ProjectileImpactEvent(net.minecraftforge.event.entity.ProjectileImpactEvent.Throwable event)
	{
		if(ModChecker.potionOfBeesPresent && Bumblezone.BzModCompatibilityConfig.allowPotionOfBeesCompat.get())
		{
			PotionOfBeesCompat.POBReviveLarvaBlockEvent(event);
		}
	}
}
