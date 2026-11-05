package com.lingvadesk.app.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lingvadesk.app.domain.repository.LingvaRepository
import com.lingvadesk.app.domain.study.Option
import com.lingvadesk.app.domain.study.PrefKeys
import com.lingvadesk.app.domain.study.PrefOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Общие настройки приложения (таблица `settings`): тема, шрифт, лимиты, озвучка. */
@HiltViewModel
class AppViewModel @Inject constructor(private val repo: LingvaRepository) : ViewModel() {
    private val _settings = MutableStateFlow(PrefKeys.defaults)
    val settings: StateFlow<Map<String, String>> = _settings.asStateFlow()

    init { reload() }

    fun reload() {
        viewModelScope.launch { _settings.value = PrefKeys.defaults + repo.settings() }
    }

    fun set(key: String, value: String) {
        _settings.value = _settings.value + (key to value)
        viewModelScope.launch { repo.setSetting(key, value) }
    }

    /** Нажатие плитки: следующее значение по кругу. */
    fun cycle(key: String, options: List<Option>) = set(key, PrefOptions.next(options, _settings.value[key]).value)
}
