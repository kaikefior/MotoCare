# MotoCare

Aplicativo Android para gerenciamento de motocicletas, desenvolvido para o Projeto Final de Desenvolvimento Mobile II.

## Integrantes da dupla

- Integrante 1: [Nome do integrante 1]
- Integrante 2: [Nome do integrante 2]

## Proposta

O MotoCare tem como proposta permitir o cadastro de motos, o registro de abastecimentos e manutenções, o acompanhamento da quilometragem e dos gastos e a consulta ao histórico de serviços e às próximas manutenções.

## Estado atual

- Login local com sessão e cadastro de usuários.
- Cadastro, listagem, detalhes, edição e exclusão de motocicletas por usuário.
- Persistência das motos com Room, consultas reativas com Flow e operações suspend.
- Validações de campos obrigatórios, ano, quilometragem e placa; bloqueio de placa duplicada na mesma conta.
- Lista de motos em um Fragment, com RecyclerView e View Binding.
- Abastecimentos vinculados à moto, com cadastro, histórico, edição e exclusão.
- Total calculado a partir dos litros e preço por litro; resumo de gastos e litros.
- Atualização da quilometragem da moto quando o abastecimento registra uma leitura maior.
- Manutenções realizadas com serviço, data, quilometragem, custo e observações.
- Cadastro, edição, exclusão e histórico de manutenções por moto.
- Resumo dos gastos acumulados com combustível e manutenção.
- Previsões opcionais por data ou quilometragem no formulário de manutenção.
- Próximas manutenções classificadas como em dia, próximas ou atrasadas.
- Aviso de proximidade até 7 dias ou 500 km; previsão pode ser marcada como concluída.
- Banco Room na versão 4, com migrações automáticas que preservam os registros.
- Contas de usuário permanecem no SQLiteOpenHelper. Os bancos existentes são preservados.

## Pendências para entrega

- Conferência manual das telas e das migrações no Android Studio.
- Prints, integrantes e configuração do GitHub.

## Tecnologias

- Kotlin e layouts XML.
- Android Studio para compilação, execução e depuração; VS Code para edição.
- Android SDK: minSdk 24, targetSdk 36 e compileSdk 37.
- Activities, Fragment, Flow, RecyclerView, View Binding, ConstraintLayout e componentes Material.
- Room com KSP para motos; SQLiteOpenHelper para o login.

## Como executar

1. Abra a pasta raiz MotoCare no Android Studio, onde está settings.gradle.kts.
2. Aguarde a sincronização do Gradle e instale os componentes do SDK solicitados.
3. Selecione a configuração app e um emulador ou dispositivo físico.
4. Execute Run 'app'.
5. Crie uma conta ou use o login de demonstração: admin@admin.com / 123456.

Para editar no VS Code, abra a mesma pasta, salve as alterações e execute novamente pelo Android Studio.

### Verificações pelo terminal

```powershell
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest
.\gradlew.bat :app:lintDebug
```

Os testes locais cobrem as validações de motos. Os testes instrumentados cobrem CRUD, isolamento entre usuários, duplicidade de placa e persistência ao reabrir o banco; execute-os em emulador ou dispositivo com `.\gradlew.bat :app:connectedDebugAndroidTest`.

## Identificação do aplicativo

- Nome do projeto e do aplicativo: MotoCare.
- Pacote Kotlin, namespace e applicationId: com.example.motocare.
- Room para motos: motocare_motos.db.
- Banco de usuários: motocare.db. Tabelas antigas já existentes são preservadas.

A alteração do applicationId faz o Android tratar esta versão como outro aplicativo. Os dados da instalação anterior não são transferidos automaticamente.

## Capturas das telas

Pendente: adicionar um arquivo com capturas reais das telas para a entrega.

## GitHub e colaboração

Pendente: configurar o repositório real, preencher os integrantes e registrar o desenvolvimento da dupla com commits e branches.

O professor jaqsondalbosco deverá ter acesso ao repositório; esse acesso ainda precisa ser verificado no GitHub.
