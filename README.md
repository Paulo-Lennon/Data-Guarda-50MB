# Data Guard 150

Aplicativo Android nativo que monitora o consumo diário de dados móveis e exibe um alerta sobre outros aplicativos quando o total do dia alcança 150 MB.

## Comportamento
- Limite: 150 MiB por dia (150 × 1024 × 1024 bytes).
- Ao ultrapassar o limite, mostra: **“Atenção: Está usando o Wifi !?”**
- O alerta possui somente o botão **Fechar**.
- O alerta é mostrado no máximo uma vez por dia.
- O monitoramento é executado pelo WorkManager em intervalos de aproximadamente 15 minutos, reduzindo uso de bateria.

## Tecnologias
- Kotlin
- Android SDK / Android Studio
- NetworkStatsManager
- WorkManager
- WindowManager + TYPE_APPLICATION_OVERLAY
- SharedPreferences
- Material Components

## Permissões do usuário
1. Acesso de uso (`PACKAGE_USAGE_STATS`) pela tela de Configurações do Android.
2. Exibir sobre outros apps (`SYSTEM_ALERT_WINDOW`).

## Abrir
Abra a pasta no Android Studio, aguarde a sincronização do Gradle, conecte um Android físico e execute o módulo `app`.

## Observações
A API de estatísticas do Android trabalha com dados agregados e o WorkManager não é um cronômetro exato. Por isso, o alerta pode aparecer alguns minutos depois de o aparelho efetivamente atingir 150 MB. Fabricantes também podem aplicar políticas extras de economia de bateria.
