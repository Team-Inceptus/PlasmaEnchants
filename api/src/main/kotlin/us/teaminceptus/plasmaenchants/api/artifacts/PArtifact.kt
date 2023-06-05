package us.teaminceptus.plasmaenchants.api.artifacts

import org.bukkit.Keyed
import us.teaminceptus.plasmaenchants.api.PTarget

/**
 * Represents an artifact that can be applied to an item.
 */
interface PArtifact : Keyed {

    /**
     * Fetches the human-readable name of this artifact.
     * @return Name of artifact
     */
    fun getName(): String

    /**
     * Fetches the human-readable description of this artifact.
     * @return Description of artifact
     */
    fun getDescription(): String

    /**
     * Fetches the Target Type of this artifact.
     * @return Target Type
     */
    fun getTarget(): PTarget
}