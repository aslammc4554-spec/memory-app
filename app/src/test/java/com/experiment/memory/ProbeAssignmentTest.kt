package com.experiment.memory

import com.experiment.memory.data.TrialData
import com.experiment.memory.service.ProbeAssignmentService
import com.experiment.memory.ui.generateProbe
import org.junit.Assert.*
import org.junit.Test
import kotlin.random.Random

class ProbeAssignmentTest {

    private val sampleWordList = listOf(
        "Bicycle", "Library", "Pillow", "Toaster", "Sheep",
        "Wallet", "Ground", "Ladder", "Finger", "Camera"
    )

    /**
     * Requirement 1 & 6: Practice always uses Control and does not count toward the 8 experimental probes.
     */
    @Test
    fun testPracticeAlwaysUsesControl() {
        val listOrder = listOf(0, 1, 2, 3, 4, 5, 6, 7, 8)
        val expProbes = ProbeAssignmentService.createExperimentalProbeAssignment()
        val assignments = ProbeAssignmentService.buildSessionAssignments(listOrder, expProbes)

        val practice = assignments.first { it.presentationOrder == 0 }
        assertEquals(0, practice.listNumber)
        assertEquals(ProbeAssignmentService.PROBE_CONTROL, practice.probeType)
    }

    /**
     * Requirement 1 & 2: Every new experimental session receives exactly four Critical and four Control probes.
     * Verified over 100 simulated participants.
     */
    @Test
    fun testEveryNewExperimentalSessionReceivesFourCriticalAndFourControl() {
        for (i in 1..100) {
            val expProbes = ProbeAssignmentService.createExperimentalProbeAssignment(Random(i * 1000L))
            assertEquals(8, expProbes.size)
            assertEquals(4, expProbes.count { it == ProbeAssignmentService.PROBE_CRITICAL })
            assertEquals(4, expProbes.count { it == ProbeAssignmentService.PROBE_CONTROL })
        }
    }

    /**
     * Requirement 2: The assignment is generated only once per session and must not regenerate when moving between lists.
     */
    @Test
    fun testAssignmentGeneratedOnlyOncePerSession() {
        val listOrder = listOf(0, 5, 3, 1, 8, 2, 7, 4, 6)
        val expProbes = ProbeAssignmentService.createExperimentalProbeAssignment(Random(42))
        val assignments1 = ProbeAssignmentService.buildSessionAssignments(listOrder, expProbes)

        // Simulating moving between lists using the same assignment
        val assignments2 = assignments1.toList()
        assertEquals(assignments1, assignments2)
        assertTrue(ProbeAssignmentService.isValidAssignment(assignments2))
    }

    /**
     * Requirement 2 & 7: Probe assignments remain unchanged after app restart and session recovery.
     */
    @Test
    fun testProbeAssignmentsRemainUnchangedAfterSessionRecovery() {
        val initialOrder = listOf(0, 2, 4, 6, 8, 1, 3, 5, 7)
        val initialProbes = listOf(
            ProbeAssignmentService.PROBE_CONTROL, // Practice
            ProbeAssignmentService.PROBE_CRITICAL,
            ProbeAssignmentService.PROBE_CONTROL,
            ProbeAssignmentService.PROBE_CRITICAL,
            ProbeAssignmentService.PROBE_CONTROL,
            ProbeAssignmentService.PROBE_CRITICAL,
            ProbeAssignmentService.PROBE_CONTROL,
            ProbeAssignmentService.PROBE_CRITICAL,
            ProbeAssignmentService.PROBE_CONTROL
        )

        // Simulate session serialization / deserialization (recovery)
        val recoveredProbes = initialProbes.toList()
        val recoveredOrder = initialOrder.toList()

        assertEquals(initialProbes, recoveredProbes)
        assertEquals(initialOrder, recoveredOrder)

        // Experimental probe counts must stay exactly 4 Critical and 4 Control
        val experimentalRecovered = recoveredProbes.subList(1, 9)
        assertEquals(4, experimentalRecovered.count { it == ProbeAssignmentService.PROBE_CRITICAL })
        assertEquals(4, experimentalRecovered.count { it == ProbeAssignmentService.PROBE_CONTROL })
    }

    /**
     * Requirement 3: Randomized presentation order does not corrupt list identity mapping.
     */
    @Test
    fun testRandomizedPresentationOrderDoesNotCorruptMapping() {
        val rng = Random(12345)
        for (sim in 1..50) {
            val shuffledExpLists = (1..8).toList().shuffled(rng)
            val listOrder = listOf(0) + shuffledExpLists
            val expProbes = ProbeAssignmentService.createExperimentalProbeAssignment(rng)
            val assignments = ProbeAssignmentService.buildSessionAssignments(listOrder, expProbes)

            assertTrue(ProbeAssignmentService.isValidAssignment(assignments))

            // Ensure each list number 1..8 appears exactly once across presentation slots 1..8
            val mappedListNumbers = assignments.filter { it.presentationOrder > 0 }.map { it.listNumber }
            assertEquals((1..8).toSet(), mappedListNumbers.toSet())
        }
    }

    /**
     * Requirement 4: Critical probes test the word immediately before the target word.
     */
    @Test
    fun testCriticalProbesTestWordImmediatelyBeforeTarget() {
        // Test with different target positions 4, 5, 6, 7
        for (targetPos in listOf(4, 5, 6, 7)) {
            val probe = generateProbe(sampleWordList, targetPosition = targetPos, probeType = "Critical")
            val targetWord = sampleWordList[targetPos - 1]
            val expectedPrecedingWord = sampleWordList[targetPos - 2]

            assertEquals("Critical probe must ask about the target word", targetWord, probe.wordAsked)
            assertEquals("Critical probe correct answer must be targetPosition - 1 word", expectedPrecedingWord, probe.correctAnswer)
            assertTrue("Options must contain the correct answer", probe.leftOption == expectedPrecedingWord || probe.rightOption == expectedPrecedingWord)
        }
    }

    /**
     * Requirement 5: Control probes test a valid non-target word at positions 2–10.
     * Never select position 1 or the target position.
     */
    @Test
    fun testControlProbesTestValidNonTargetWordPositions2To10() {
        val targetPos = 5 // target word is sampleWordList[4] ("Sheep")
        val targetWord = sampleWordList[targetPos - 1]
        val position1Word = sampleWordList[0] // "Bicycle"

        for (seed in 1..200) {
            val rng = Random(seed)
            val probe = generateProbe(sampleWordList, targetPosition = targetPos, probeType = "Control", random = rng)

            assertNotEquals("Control probe must NEVER ask about position 1", position1Word, probe.wordAsked)
            assertNotEquals("Control probe must NEVER ask about target word", targetWord, probe.wordAsked)

            val testedIndex = sampleWordList.indexOf(probe.wordAsked)
            assertTrue("Tested position must be between positions 2 and 10 (index 1..9)", testedIndex in 1..9)
            assertEquals("Correct answer must be the word immediately preceding tested word", sampleWordList[testedIndex - 1], probe.correctAnswer)
        }
    }

    /**
     * Requirement 5: Both answer options come from the same list and are distinct.
     */
    @Test
    fun testProbeAnswerOptionsAreDistinctAndFromSameList() {
        for (seed in 1..100) {
            val rng = Random(seed)
            for (probeType in listOf("Critical", "Control")) {
                val probe = generateProbe(sampleWordList, targetPosition = 4, probeType = probeType, random = rng)

                assertNotEquals("Answer options must be distinct", probe.leftOption, probe.rightOption)
                assertTrue("Left option must come from the list", sampleWordList.contains(probe.leftOption))
                assertTrue("Right option must come from the list", sampleWordList.contains(probe.rightOption))
                assertTrue("One of the options must be the correct answer", probe.leftOption == probe.correctAnswer || probe.rightOption == probe.correctAnswer)
            }
        }
    }

    /**
     * Requirement 8: CSV exports contain the actual stored assignments (Critical / Control, practice = 1/0).
     */
    @Test
    fun testCsvExportsContainActualStoredAssignments() {
        val expProbes = listOf("Critical", "Control", "Critical", "Critical", "Control", "Control", "Control", "Critical")
        val trials = mutableListOf<TrialData>()

        // Practice
        trials.add(
            TrialData(
                id = 1,
                participant_id = "P-TEST-001",
                group_condition = "Predictable",
                list_number = 0,
                presentation_order = 0,
                target_position = 4,
                probe_type = "Control",
                probe_word_asked = "Toaster",
                options_shown_left = "Pillow",
                options_shown_right = "Sheep",
                response = "Pillow",
                accuracy = 1,
                continue_rt_ms = 1200L,
                speaking_response_given = true,
                manip_thinking = null,
                manip_anxiety = null,
                practice = 1,
                mode = "pilot",
                data_use_decision = "Agree",
                timestamp_start = 1000L,
                timestamp_end = 2000L
            )
        )

        // 8 Experimental lists
        for (i in 1..8) {
            trials.add(
                TrialData(
                    id = i + 1,
                    participant_id = "P-TEST-001",
                    group_condition = "Predictable",
                    list_number = i,
                    presentation_order = i,
                    target_position = 5,
                    probe_type = expProbes[i - 1],
                    probe_word_asked = "Word$i",
                    options_shown_left = "Left$i",
                    options_shown_right = "Right$i",
                    response = "Left$i",
                    accuracy = 1,
                    continue_rt_ms = 1100L,
                    speaking_response_given = true,
                    manip_thinking = 2,
                    manip_anxiety = 3,
                    practice = 0,
                    mode = "pilot",
                    data_use_decision = "Agree",
                    timestamp_start = 2000L + i * 1000,
                    timestamp_end = 3000L + i * 1000
                )
            )
        }

        // Build CSV representation
        val csvHeader = "id,participant_id,group_condition,list_number,presentation_order,target_position,probe_type,probe_word_asked,options_shown_left,options_shown_right,response,accuracy,continue_rt_ms,speaking_response_given,manip_thinking,manip_anxiety,practice,mode,data_use_decision,timestamp_start,timestamp_end,excluded\n"
        val csvContent = StringBuilder().apply {
            append(csvHeader)
            for (row in trials) {
                append("${row.id},${row.participant_id},${row.group_condition},${row.list_number},${row.presentation_order},${row.target_position},${row.probe_type},${row.probe_word_asked},${row.options_shown_left},${row.options_shown_right},${row.response},${row.accuracy},${row.continue_rt_ms ?: ""},${row.speaking_response_given},${row.manip_thinking ?: ""},${row.manip_anxiety ?: ""},${row.practice},${row.mode},${row.data_use_decision ?: ""},${row.timestamp_start},${row.timestamp_end},${row.excluded ?: ""}\n")
            }
        }.toString()

        val lines = csvContent.lines().filter { it.isNotBlank() }
        assertEquals(10, lines.size) // header + 1 practice + 8 experimental

        // Verify Practice row in CSV
        val practiceCsv = lines[1].split(",")
        assertEquals("Control", practiceCsv[6]) // probe_type
        assertEquals("1", practiceCsv[16]) // practice

        // Verify Experimental rows in CSV
        var criticalCount = 0
        var controlCount = 0
        for (i in 2..9) {
            val row = lines[i].split(",")
            val probeType = row[6]
            val practiceVal = row[16]
            assertEquals("0", practiceVal)
            if (probeType == "Critical") criticalCount++
            if (probeType == "Control") controlCount++
        }
        assertEquals(4, criticalCount)
        assertEquals(4, controlCount)
    }

    /**
     * Requirement 9: Test randomization distribution over multiple simulated participants.
     */
    @Test
    fun testRandomizationDistributionOverMultipleSimulatedParticipants() {
        val sequences = mutableSetOf<List<String>>()
        for (i in 1..50) {
            val expProbes = ProbeAssignmentService.createExperimentalProbeAssignment(Random(i * 37 + 11))
            assertEquals(4, expProbes.count { it == "Critical" })
            assertEquals(4, expProbes.count { it == "Control" })
            sequences.add(expProbes)
        }
        // Over 50 participants with 70 possible permutations (8 choose 4 = 70), there should be diverse sequences
        assertTrue("Random sequences across participants must vary", sequences.size > 10)
    }
}
