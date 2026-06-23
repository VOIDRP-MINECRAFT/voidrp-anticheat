# 🛡️ VoidRP Anticheat

> Серверный NeoForge мод — движение, бой, детект инжекций и снимки модов клиента.

![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1-brightgreen?logo=minecraft)
![NeoForge](https://img.shields.io/badge/NeoForge-21.1.x-orange)
![Java](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white)
![License](https://img.shields.io/badge/license-proprietary-red)

---

## 🗺️ Место в экосистеме

```
  Minecraft Client
        │ ModListPayload · InjectionReportPayload (C2S пакеты)
        ▼
  voidrp-anticheat (NeoForge, сервер)
        │ POST /api/v1/anticheat/* (X-Game-Auth-Secret)
        ▼
  minecraft-backend → Admin Panel (просмотр нарушений)
```

---

## ✨ Возможности

### Проверки движения и боя

| Чек | Что ловит |
|---|---|
| **SpeedCheck** | скорость передвижения выше порога |
| **FlyCheck** | полёт без разрешения (с учётом зелий, воды, элитры) |
| **ReachCheck** | дистанция атаки > порога |
| **KillAuraCheck** | атака цели за спиной (угол > 107°) |
| **CpsCheck** | кликов в секунду выше порога |

### Система нарушений (Violation Level)
- VL накапливается на сервере per-player, per-check
- При достижении `vlThreshold` → отчёт отправляется в backend, VL сбрасывается
- VL уменьшается на 1 каждые 5 секунд
- Репорты throttle: раз в 10 секунд на тип чека

### Проверка целостности клиента
- **ModListPayload** — клиент отправляет список модов при логине → сервер передаёт в backend для сравнения с вердиктами
- **InjectionReportPayload** — клиент сообщает об обнаруженных JVM-агентах и подозрительных нативных библиотеках

---

## 📋 Требования

| Компонент | Версия |
|---|---|
| Minecraft | 1.21.1 |
| NeoForge | 21.1.x |
| Java | 21 |

Мод устанавливается **только на сервер**. Клиентам нужна клиентская часть для отправки C2S пакетов.

---

## 🚀 Сборка

```bash
cd voidrp_anticheat
./gradlew jar
# → build/libs/voidrp_anticheat-1.0.0.jar
```

---

## ⚙️ Конфигурация

`config/voidrp_anticheat-server.toml`:

```toml
[general]
enabled = true
backendUrl = "https://api.void-rp.ru/api/v1"
gameAuthSecret = "секрет"
vlThreshold = 10

[checks]
speedThreshold = 0.35          # блоки/тик
flyTicksThreshold = 20         # тиков в воздухе
reachThreshold = 3.5           # блоков
killauraTargetsPerSecond = 8
cpsThreshold = 20
```

---

## 🔗 Связанные репозитории

| Репо | Связь |
|---|---|
| [minecraft-backend](https://github.com/VOIDRP-MINECRAFT/minecraft-backend) | Принимает отчёты `/anticheat/violation`, `/anticheat/mod-snapshot` |
| [voidrp-site](https://github.com/VOIDRP-MINECRAFT/voidrp-site) | Admin Panel — просмотр нарушений и вердикты по модам |

---

<div align="center">
<a href="https://void-rp.ru">🌐 Сайт</a> ·
<a href="https://github.com/VOIDRP-MINECRAFT">🏠 Организация</a>
</div>
