# 🛡️ VoidRP Anticheat

> NeoForge-мод VoidRP: серверные проверки движения и боя с накоплением нарушений (VL), снимок модов клиента,
> поиск инжектов в JVM и пороги, которые админ меняет в панели без рестарта.

![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1%20%7C%2026.2-brightgreen?logo=minecraft)
![NeoForge](https://img.shields.io/badge/NeoForge-21.1%20%7C%2026.2-orange)
![Java](https://img.shields.io/badge/Java-21%20%7C%2025-ED8B00?logo=openjdk&logoColor=white)
[![Build](https://github.com/VOIDRP-MINECRAFT/voidrp-anticheat/actions/workflows/build.yml/badge.svg)](https://github.com/VOIDRP-MINECRAFT/voidrp-anticheat/actions/workflows/build.yml)
![License](https://img.shields.io/badge/license-proprietary-red)

---

## 🗺️ Место в экосистеме

```mermaid
flowchart LR
    subgraph CL["Клиент + voidrp-anticheat"]
        ML["Список модов"]
        INJ["InjectionDetector<br/>JVM-агенты, нативные библиотеки"]
    end
    subgraph SV["Сервер + voidrp-anticheat"]
        CH["Проверки<br/>Speed · Fly · Reach · KillAura · CPS"]
        VL["VL на игрока и проверку"]
        RP["Статус ресурспака"]
    end
    B[("minecraft-backend<br/>/api/v1/anticheat/*")]
    ADM["🧰 Админ-панель сайта"]

    ML -- "ModListPayload" --> SV
    INJ -- "InjectionReportPayload" --> SV
    CH --> VL
    VL -- "порог → violation" --> B
    SV -- "mod-snapshot · injection-report" --> B
    B -- "пороги из админки<br/>/anticheat/config" --> CH
    B --> ADM
    ADM -- "вердикты по модам" --> B
```

---

## ✨ Возможности

### Проверки

| Проверка | Что ловит | Порог по умолчанию |
|---|---|---|
| **SpeedCheck** | Скорость передвижения выше нормы | `0.75` блока/тик |
| **FlyCheck** | Полёт без разрешения (учитывает зелья, воду, элитры) | `40` тиков в воздухе |
| **ReachCheck** | Удар дальше допустимого | `6.5` блока |
| **KillAuraCheck** | Слишком много целей в секунду и удары по цели за спиной (угол больше ~107°) | `6` целей/с |
| **CpsCheck** | Кликов в секунду выше нормы | `25` CPS |

### Нарушения (Violation Level)

```mermaid
flowchart LR
    F["Проверка сработала"] --> V["VL + 1<br/>на игрока и тип"]
    V --> Q{"VL ≥ vlThreshold<br/>(10)?"}
    Q -- нет --> D["каждые 5 с VL − 1"]
    Q -- да --> R{"прошло ≥ 10 с<br/>с прошлого отчёта?"}
    R -- да --> S["📤 отчёт в бэкенд<br/>VL сброшен"]
    R -- нет --> D
```

### Целостность клиента
- **ModListPayload** — клиент при входе присылает список модов; бэкенд сверяет его с вердиктами админов.
- **InjectionReportPayload** — клиент сообщает о JVM-агентах и подозрительных нативных библиотеках.
- **Статус ресурспака** — сервер запоминает ответ клиента на серверный ресурспак и прикладывает его к отчётам.

### Пороги из админки
`RemoteConfigManager` получает пороги с бэкенда (`GET /api/v1/anticheat/config`) и применяет их без рестарта;
пока ответа нет, действуют значения из TOML.

---

## 📋 Требования

| Сборка | Minecraft | NeoForge | Java |
|---|---|---|---|
| по умолчанию | 1.21.1 | 21.1.x | 21 |
| `-PmcVer=26.2` | 26.2 | 26.2 | 25 |

Мод ставится **и на сервер, и на клиент**: клиентская часть присылает список модов и отчёт об инжектах.
Версионно-зависимый код — в `src/versions/`.

---

## 🚀 Сборка

```bash
./gradlew build                  # 1.21.1
./gradlew jar -PmcVer=26.2       # 26.2
```

---

## ⚙️ Конфигурация

`config/voidrp_anticheat-server.toml`:

```toml
[general]
enabled = true
backendUrl = "https://api.void-rp.ru"
gameAuthSecret = ""        # X-Game-Auth-Secret этого сервера — только на сервере
serverSlug = ""
vlThreshold = 10

[checks]
speedThreshold = 0.75
flyTicksThreshold = 40
reachThreshold = 6.5
killauraTargetsPerSecond = 6
cpsThreshold = 25
```

---

## 🔗 Связанные репозитории

| Репо | Связь |
|---|---|
| [minecraft-backend](https://github.com/VOIDRP-MINECRAFT/minecraft-backend) | Принимает `/anticheat/violation`, `/anticheat/mod-snapshot`, `/anticheat/injection-report`, отдаёт `/anticheat/config` |
| [voidrp-site](https://github.com/VOIDRP-MINECRAFT/voidrp-site) | Админ-панель: нарушения, вердикты по модам, пороги |

---

<div align="center">
<a href="https://void-rp.ru">🌐 Сайт</a> ·
<a href="https://github.com/VOIDRP-MINECRAFT">🏠 Организация</a>
</div>
