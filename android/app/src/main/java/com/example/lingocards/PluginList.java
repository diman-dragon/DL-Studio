package com.example.lingocards;

import com.example.lingocards.backup.BackupCreatePlugin;
import com.example.lingocards.backup.BackupRestorePlugin;
import com.example.lingocards.db.DbBatchPlugin;
import com.example.lingocards.db.DbQueryPlugin;
import com.example.lingocards.db.DbRunPlugin;
import com.example.lingocards.saf.SafOpenPlugin;
import com.example.lingocards.anki.AnkiNotesPlugin;
import com.example.lingocards.icon.IconLevelPlugin;
import com.example.lingocards.screen.KeepAwakePlugin;
import com.example.lingocards.speech.SpeechListenPlugin;
import com.example.lingocards.saf.SafSavePlugin;
import com.example.lingocards.tts.TtsSpeakPlugin;
import com.example.lingocards.tts.TtsStopPlugin;
import com.getcapacitor.Plugin;
import java.util.Arrays;
import java.util.List;

/** Реестр плагинов: добавить плагин = добавить строку здесь. */
public final class PluginList {
    public static final List<Class<? extends Plugin>> ALL = Arrays.<Class<? extends Plugin>>asList(
        DbQueryPlugin.class, DbRunPlugin.class, DbBatchPlugin.class,
        SafOpenPlugin.class, SafSavePlugin.class,
        BackupCreatePlugin.class, BackupRestorePlugin.class,
        TtsSpeakPlugin.class, TtsStopPlugin.class, KeepAwakePlugin.class, SpeechListenPlugin.class, AnkiNotesPlugin.class, IconLevelPlugin.class);
}
