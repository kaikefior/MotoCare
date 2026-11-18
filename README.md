# MotoCare

O MotoCare é um aplicativo Android desenvolvido em Kotlin para o gerenciamento de motocicletas, registros de abastecimento, acompanhamento de manutenções e controle de gastos.

Este aplicativo foi desenvolvido como Projeto Final da disciplina de Desenvolvimento Mobile II do curso de Análise e Desenvolvimento de Sistemas da Universidade de Passo Fundo (UPF).

---

## Desenvolvedor

* Kaike Fior - Análise e Desenvolvimento de Sistemas (UPF)

Projeto desenvolvido individualmente para a disciplina de Desenvolvimento Mobile II.

---

## Descrição da Proposta

O MotoCare visa facilitar o dia a dia de motociclistas, centralizando informações essenciais sobre a manutenção e o uso do veículo.

### Principais Funcionalidades:
- Autenticação de Usuários: Tela de login funcional com suporte a cadastro de novos usuários e conta de demonstração.
- Gerenciamento de Motocicletas (CRUD): Cadastro, listagem, edição e exclusão de motos com validação de placa (padrão antigo e Mercosul), ano e quilometragem.
- Controle de Abastecimentos (CRUD): Registro de data, quilometragem, litros e preço por litro, calculando automaticamente o custo total e atualizando o odômetro do veículo.
- Histórico de Manutenções (CRUD): Registro de serviços (troca de óleo, revisões, pneus, etc.) com valores, observações e controle de revisões concluídas.
- Previsão de Próximas Manutenções: Alertas e acompanhamento de revisões futuras por prazo de data ou por quilometragem excedida.
- Resumo Financeiro: Cálculo automático do total gasto em combustível, manutenções e custo geral do veículo.

---

## Tecnologias Utilizadas

* Linguagem: Kotlin
* IDE: Android Studio
* Interface (UI): Layouts em XML, View Binding, Material Design 3 e suporte a Edge-to-Edge (WindowInsets)
* Persistência de Dados:
  * Room Database (SQLite): Tabelas de motos, abastecimentos e manutenções com chaves estrangeiras e deleção em cascata (CASCADE).
  * SQLite Native (SQLiteOpenHelper): Autenticação e cadastro de usuários.
  * SharedPreferences: Gerenciamento de sessão ativa (SessionManager).
* Concorrência e Reatividade: Kotlin Coroutines e Flow integrado ao ciclo de vida (repeatOnLifecycle).
* Testes: Unitários (JUnit) e Testes de DAO/Room (AndroidJUnit4).

---

## Instruções de Execução

### Pré-requisitos
* Android Studio (versão Iguana, Jellyfish, Ladybug ou superior recomendada).
* JDK 11 ou superior configurado no Android Studio.
* Emulador Android ou dispositivo físico rodando Android 7.0 (API 24) ou superior.

### Passo a Passo para Executar

1. Clonar o Repositório:
   ```bash
   git clone https://github.com/SEU_USUARIO/MotoCare.git
   ```

2. Abrir o Projeto no Android Studio:
   - Abra o Android Studio.
   - Clique em File > Open... e selecione a pasta raiz do projeto MotoCare.
   - Aguarde o término do Gradle Sync para baixar as dependências.

3. Executar a Aplicação:
   - Selecione um emulador ativo ou conecte um dispositivo físico via Depuração USB.
   - Clique no botão Run 'app' (Shift + F10 ou no ícone verde de execução na barra superior).

### Credenciais para Teste Rápido
Para testar a aplicação sem criar um novo cadastro, utilize a conta de demonstração integrada:
* E-mail: admin@admin.com
* Senha: 123456

---

## Prints das Telas

| Tela de Login | Tela de Registro | Lista de Motos |
| :---: | :---: | :---: |
| *(Adicione a imagem aqui)* | *(Adicione a imagem aqui)* | *(Adicione a imagem aqui)* |

| Formulário da Moto | Detalhes e Gastos | Cadastro de Abastecimento |
| :---: | :---: | :---: |
| *(Adicione a imagem aqui)* | *(Adicione a imagem aqui)* | *(Adicione a imagem aqui)* |

> Dica: Você pode salvar os prints na pasta docs/screenshots do repositório e referenciá-los aqui utilizando markdown ![Nome da Tela](docs/screenshots/nome_da_imagem.png).

---

## Disciplina
* Curso: Análise e Desenvolvimento de Sistemas — UPF
* Disciplina: Desenvolvimento Mobile II
* Professor: Jaqson Dalbosco
