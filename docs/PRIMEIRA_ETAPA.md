# MotoCare: primeira etapa

## Fluxo do código

XML da tela -> Activity ou Fragment -> DAO -> banco Room.

A proposta é manter o projeto pequeno e fácil de explicar.
As telas acessam o DAO diretamente.

## Arquivos principais

- model/Moto.kt: os campos de uma moto e a tabela do banco.
- database/MotoDao.kt: cadastrar, buscar, editar e excluir; também confere placa repetida.
- database/MotoCareDatabase.kt: abre o banco Room.
- MotoFormActivity.kt: lê o formulário, valida os campos e salva.
- MotoDetailActivity.kt: mostra os dados e oferece editar e excluir.
- ui/MotoListFragment.kt: mostra e atualiza a lista de motos.
- adapter/MotoAdapter.kt: coloca os dados de cada moto no cartão XML.
- validation/MotoValidator.kt: regras simples de ano, quilometragem e placa.
- session/SessionManager.kt: guarda o ID do usuário que entrou.

Os caminhos acima ficam em app/src/main/java/com/example/motocare.

## Anotações do Room

- @Entity: esta classe representa uma tabela.
- @PrimaryKey: identifica cada cadastro; autoGenerate gera o número.
- @Dao: esta classe concentra o acesso ao banco.
- @Query: o Room executa o SQL informado.
- @Insert: insere um cadastro.
- @Transaction: executa a conferência e a gravação como uma única operação.

As funções suspend permitem aguardar o banco sem travar a interface.
Flow atualiza a tela quando os cadastros mudam.
View Binding permite acessar os componentes do XML pelos seus IDs.

Referência: https://developer.android.com/training/data-storage/room

## Teste manual no Android Studio

1. Sincronize o Gradle e execute app.
2. Crie uma conta ou use admin@admin.com / 123456.
3. Cadastre uma moto pelo botão +.
4. Abra os detalhes e edite o cadastro.
5. Feche e reabra o app para conferir a persistência.
6. Use Sair e entre em outra conta: a garagem deve estar vazia.
7. Volte à primeira conta e exclua o cadastro.
8. Teste campos vazios, ano inválido, placa repetida e rotação da tela.

## Próximas etapas

1. Implementar abastecimentos vinculados a uma moto.
2. Cadastrar e consultar manutenções.
3. Mostrar as próximas manutenções por data ou quilometragem.
4. Finalizar README, prints e colaboração pelo GitHub.

As motos usam Room em motocare_motos.db.
Os usuários continuam em motocare.db. Tabelas antigas existentes não são apagadas.
