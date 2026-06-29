package com.example.patent

interface IDispatchTarget {
    val id: String
    val displayName: String
    val primaryContact: String
    fun matches(emergencyType: String): Boolean
}

interface ISmartDispatchEngine {
    fun resolveDispatchRoute(emergencyType: String): List<IDispatchTarget>
    fun registerDispatchTarget(target: IDispatchTarget)
}

class HospitalDispatchTarget : IDispatchTarget {
    override val id = "VISAKHA_HOSPITAL"
    override val displayName = "Visakha Hospital Trauma Care"
    override val primaryContact = "108 / Emergency ER Desk"
    override fun matches(emergencyType: String): Boolean {
        return emergencyType == "Medical" || emergencyType == "Accident"
    }
}

class FireDispatchTarget : IDispatchTarget {
    override val id = "DUVVADA_FIRE_DEPT"
    override val displayName = "Duvvada Fire Station"
    override val primaryContact = "101 / Dispatcher"
    override fun matches(emergencyType: String): Boolean {
        return emergencyType == "Fire"
    }
}

class PoliceDispatchTarget : IDispatchTarget {
    override val id = "DUVVADA_POLICE"
    override val displayName = "Duvvada Police Department"
    override val primaryContact = "100 / Patrol Control"
    override fun matches(emergencyType: String): Boolean {
        return emergencyType == "Accident" || emergencyType == "Violence"
    }
}

class DisasterDispatchTarget : IDispatchTarget {
    override val id = "NDRF_DISASTER"
    override val displayName = "Disaster Response Team"
    override val primaryContact = "1078 / NDRF Base"
    override fun matches(emergencyType: String): Boolean {
        return emergencyType == "Natural Disaster"
    }
}

class SmartDispatchEngineImpl : ISmartDispatchEngine {
    private val targets = mutableListOf<IDispatchTarget>()

    init {
        // Default seed targets
        targets.add(HospitalDispatchTarget())
        targets.add(FireDispatchTarget())
        targets.add(PoliceDispatchTarget())
        targets.add(DisasterDispatchTarget())
    }

    override fun resolveDispatchRoute(emergencyType: String): List<IDispatchTarget> {
        return targets.filter { it.matches(emergencyType) }
    }

    override fun registerDispatchTarget(target: IDispatchTarget) {
        if (targets.none { it.id == target.id }) {
            targets.add(target)
        }
    }
}
