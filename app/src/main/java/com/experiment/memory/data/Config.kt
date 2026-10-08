package com.experiment.memory.data

data class ExperimentConfig(
    val mode: String = "pilot",
    val sample_size: Int = 100,
    val word_interval_ms: Long = 2000,
    val distractor_duration_ms: Long = 20000,
    val target_positions: List<Int> = listOf(4, 5, 6, 7),
    val randomize_list_order: Boolean = true,
    val contact_name: String = "[Name]",
    val contact_email: String = "[Email]"
)

data class WordLists(
    val practice: List<String> = emptyList(),
    val list_1: List<String> = emptyList(),
    val list_2: List<String> = emptyList(),
    val list_3: List<String> = emptyList(),
    val list_4: List<String> = emptyList(),
    val list_5: List<String> = emptyList(),
    val list_6: List<String> = emptyList(),
    val list_7: List<String> = emptyList(),
    val list_8: List<String> = emptyList()
) {
    fun getList(index: Int): List<String> {
        return when (index) {
            0 -> practice
            1 -> list_1
            2 -> list_2
            3 -> list_3
            4 -> list_4
            5 -> list_5
            6 -> list_6
            7 -> list_7
            8 -> list_8
            else -> emptyList()
        }
    }
}
