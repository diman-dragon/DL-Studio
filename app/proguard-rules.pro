# Правила R8 для release. Compose и AndroidX приносят свои consumer-правила.
# Модели создаются через ViewModelProvider (конструктор с Application) — androidx.lifecycle сохраняет их сам.
-keepclassmembers class com.dlstudio.app.**ViewModel { <init>(...); }
