package com.experiment.memory.service

import kotlin.random.Random

data class ProbeAssignment(
    val presentationOrder: Int,
    val listNumber: Int,
    val probeType: String
)

object ProbeAssignmentService {
    const val PROBE_CRITICAL = "Critical"
    const val PROBE_CONTROL = "Control"

    /**
     * Creates a collection containing exactly four Critical and four Control values,
     * randomly shuffled.
     */
    fun createExperimentalProbeAssignment(random: Random = Random.Default): List<String> {
        val probes = mutableListOf(
            PROBE_CRITICAL, PROBE_CRITICAL, PROBE_CRITICAL, PROBE_CRITICAL,
            PROBE_CONTROL, PROBE_CONTROL, PROBE_CONTROL, PROBE_CONTROL
        )
        return probes.shuffled(random)
    }

    /**
     * Builds the mapping between presentation order, original list number, and assigned probe type.
     * Presentation order 0 is practice (list 0), always assigned Control.
     * Presentation orders 1..8 are the experimental lists, assigned the 8 experimental probes.
     */
    fun buildSessionAssignments(
        listOrder: List<Int>,
        experimentalProbes: List<String>
    ): List<ProbeAssignment> {
        require(experimentalProbes.size == 8) { "Experimental probes must contain exactly 8 items" }
        require(experimentalProbes.count { it == PROBE_CRITICAL } == 4) { "Must contain exactly 4 Critical probes" }
        require(experimentalProbes.count { it == PROBE_CONTROL } == 4) { "Must contain exactly 4 Control probes" }

        val assignments = mutableListOf<ProbeAssignment>()
        // Practice at presentation order 0 is always Control
        val practiceListNum = listOrder.getOrElse(0) { 0 }
        assignments.add(ProbeAssignment(presentationOrder = 0, listNumber = practiceListNum, probeType = PROBE_CONTROL))

        // 8 experimental lists
        for (order in 1..8) {
            val listNum = listOrder.getOrElse(order) { order }
            val probeType = experimentalProbes[order - 1]
            assignments.add(ProbeAssignment(presentationOrder = order, listNumber = listNum, probeType = probeType))
        }

        return assignments
    }

    /**
     * Validates that an assignment list satisfies all requirements:
     * - Presentation order 0 is Practice (probeType = "Control")
     * - Exactly 8 experimental lists with 4 Critical and 4 Control
     * - List numbers 1..8 all present and mapped consistently
     */
    fun isValidAssignment(assignments: List<ProbeAssignment>): Boolean {
        val practice = assignments.find { it.presentationOrder == 0 } ?: return false
        if (practice.probeType != PROBE_CONTROL) return false

        val experimental = assignments.filter { it.presentationOrder in 1..8 }
        if (experimental.size != 8) return false

        val criticalCount = experimental.count { it.probeType == PROBE_CRITICAL }
        val controlCount = experimental.count { it.probeType == PROBE_CONTROL }
        if (criticalCount != 4 || controlCount != 4) return false

        val listNumbers = experimental.map { it.listNumber }.toSet()
        if (listNumbers.size != 8) return false

        return true
    }
}
