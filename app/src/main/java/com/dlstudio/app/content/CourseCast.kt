package com.dlstudio.app.content

import com.dlstudio.app.speech.CharacterProfile
import com.dlstudio.app.speech.Characters

/** Кто действует в уроках: герои курса и первое появление персонажей на странице. */
object CourseCast {
    /** Говорящие страницы теории по порядку появления (без повторов); реплика без отметки — мисс Грейс. */
    fun onPage(page: TheoryPage): List<String> =
        TheoryBlocks.parse(page.paragraphs).filterIsInstance<Block.Say>().map { Characters.of(it.who).id }.distinct()

    /** Герои курса: все, кто говорит в теории или озвучивает вопросы тем, в порядке списка Characters. Если никого нет — пусто (курс без диалогов не знакомится). */
    fun of(topics: List<Topic>): List<CharacterProfile> {
        val ids = HashSet<String>()
        for (t in topics) {
            t.theory.forEach { ids.addAll(onPage(it)) }
            t.quiz.forEach { q -> if (q.kind.needsVoice) ids.add(Characters.of(q.who).id) }
        }
        return Characters.all.filter { it.id in ids }
    }

    /** Персонажи страницы, которых ученик ещё не встречал, в порядке появления. */
    fun fresh(page: TheoryPage, met: Set<String>): List<String> = onPage(page).filter { it !in met }
}
