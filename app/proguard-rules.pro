# Правила R8 для release. Compose и AndroidX приносят свои consumer-правила.
# Модели создаются через ViewModelProvider (конструктор с Application) — androidx.lifecycle сохраняет их сам.
-keepclassmembers class com.dlstudio.app.**ViewModel { <init>(...); }

# Читаемые отчёты о падении: имена классов и методов Compose (runtime, foundation, ui) не переименовываются,
# в стеке видно, что именно упало. Размер приложения почти не меняется (код не удаляется).
-keepattributes SourceFile,LineNumberTable
-keepnames class androidx.compose.runtime.** { *; }
-keepnames class androidx.compose.foundation.** { *; }
-keepnames class androidx.compose.ui.** { *; }
