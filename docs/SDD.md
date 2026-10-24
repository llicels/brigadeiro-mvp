# Brigadeiro — SDD (Smart Food Decision Assistant)

Versão: 1.0 · Plataforma primária: Mobile · Público inicial: pessoas com rotina
corrida que querem comer melhor, gastar menos e desperdiçar menos alimentos.

## 1. Visão do produto

### 1.1. O que é o Brigadeiro?

O Brigadeiro é um assistente de decisão alimentar que ajuda o usuário a responder:
“O que eu posso comer agora com o que eu já tenho?”.

O produto usa os ingredientes disponíveis, os equipamentos da cozinha, o tempo e o
nível de esforço desejado para apresentar opções de refeições de forma extremamente
rápida. O Brigadeiro não deve ser tratado como apenas um “gerador de receitas com IA”.
Seu principal valor é **reduzir o esforço cognitivo de decidir o que comer**.

### 1.2. Princípio central — Delivery-level convenience

O principal benchmark de experiência não é um app tradicional de receitas, é um app de
delivery: abrir, ver imagens, comparar rapidamente, considerar preço, escolher.

O Brigadeiro deve proporcionar uma experiência equivalente: abrir, ver o que pode fazer,
comparar foto + custo + tempo + disponibilidade, escolher e começar a preparar.

Regra de produto: se pedir delivery parece mais fácil do que usar o Brigadeiro, o
Brigadeiro falhou em sua principal proposta.

## 2. Problema

Pessoas com rotinas corridas chegam em casa cansadas e têm pouca disposição para decidir
o que comer. Mesmo com ingredientes em casa, podem não saber o que preparar, não lembrar
o que possuem, deixar alimentos estragarem e acabar pedindo delivery.

O problema não é falta de receitas. É a **dificuldade de transformar os ingredientes
disponíveis em uma decisão de refeição rápida, simples e adequada ao momento atual**.

## 3. Persona principal — Marina, 27 anos

Trabalha o dia todo e chega cansada. Quer comer melhor, gastar menos, desperdiçar menos e
aproveitar o que já comprou. Valoriza praticidade, rapidez, sugestões personalizadas,
economia, organização automática e ausência de julgamento.

Frase: “Quero comer melhor e gastar menos, mas no fim do dia preciso de alguém que
facilite essa decisão para mim.”

## 4. Job to be Done

Quando estou cansada e com fome no fim do dia, quero saber rapidamente o que posso comer
usando o que tenho em casa, para comer sem pensar muito, gastar menos, aproveitar meus
alimentos e manter uma alimentação melhor — sem transformar isso em mais uma obrigação.

## 5. Validações realizadas

Dificuldade de decidir o que comer; influência do cansaço; uso de delivery pela
praticidade; ingredientes em casa que não são usados; interesse em aproveitar melhor os
alimentos; necessidade de informar ingredientes de forma rápida; potencial para entradas
naturais (texto e áudio).

Insight principal: diminuir o esforço entre “estou com fome” e “sei o que vou comer”.

## 6. Hipóteses do produto

- **H1** — O problema principal é decisão.
- **H2** — Recomendações devem partir do que o usuário possui.
- **H3** — A entrada de dados deve exigir pouco esforço.
- **H4** — Tempo e esforço são diferentes (total x ativo).
- **H5** — Aparência e preço ajudam a decisão.
- **H6** — O produto não deve julgar o usuário.

## 7. Proposta de valor

O Brigadeiro transforma o que você já tem em opções fáceis de decidir e preparar.

Diferencial: combina **inventário + contexto + recomendação + praticidade** em vez de
simplesmente fornecer receitas.

## 8. Escopo do MVP

Validar o núcleo: “Tenho estes ingredientes, estes equipamentos e este nível de
tempo/esforço. O que posso fazer agora?”

### 8.1. Entrada de ingredientes

- Texto (ex.: “Tenho pão, queijo, tomate e alface.”), interpretado em ingredientes
  estruturados.
- Áudio, convertido em texto e passado pelo mesmo pipeline.

## 9. Perfil da cozinha

Durante o onboarding o usuário informa equipamentos disponíveis (fogão, forno,
micro-ondas, air fryer, liquidificador, sanduicheira, panela de pressão, mixer, outros) e
pode editar depois. **Regra:** uma receita que exige um equipamento indisponível não deve
aparecer como recomendação principal.

## 10. Descoberta de refeições

A home deve funcionar como uma vitrine de delivery: foto, nome, tempo, custo,
disponibilidade e dificuldade antes de abrir a receita.

## 11. Tempo de preparo

Diferenciar `total_time` e `active_time` (ex.: 40 min total · 10 min de preparo ativo).
Filtros: até 10 min, até 20 min, até 30 min, qualquer duração.

## 12. Custo estimado

Cada refeição apresenta um custo estimado (~R$ X por porção), deixando claro que é uma
estimativa.

## 13. Substituições inteligentes

Sugerir alternativas (ex.: pão branco → pão integral) como opções, sem julgamento e sem
alegações nutricionais exageradas ou médicas.

## 14. Aproveitamento e validade (pós-MVP)

Priorizar ingredientes que precisam ser consumidos primeiro.

## 15. Importação de compras por foto (2ª versão)

Foto da nota → OCR → parser → normalização → revisão do usuário → inventário. Itens
nunca entram silenciosamente quando a confiança é baixa.

## 16. Dashboard (posterior)

Positivo e orientado a progresso, não a culpa.

## 17. Delivery como concorrente e parceiro

O Brigadeiro ajuda o usuário a decidir; não decide moralmente o que ele deveria comer.

## 18. Mercados como stakeholders

Lista de compras, recomendações, comparação de preços, integrações e parceiros.

## 19. Arquitetura conceitual

Entidades principais: `User`, `Ingredient`, `PantryItem`, `Recipe`, `Meal`,
`ShoppingItem`.

## 20. Motor de recomendação

```
score = ingredient_match + equipment_match + time_match + effort_match
      + preference_match + freshness_priority + historical_preference + cost_fit
```

## 21. IA

Usada para interpretar texto/áudio, normalizar ingredientes, gerar/adaptar receitas,
sugerir substituições e explicar escolhas — sempre por trás de uma experiência simples.

## 22. UX / UI

1. Foto antes de texto.
2. Informação essencial primeiro.
3. Poucos passos.
4. Progressive disclosure.
5. Mobile first.
6. Estado de cansaço.

## 23. Fluxo principal do MVP

ABRIR → HOME → CARDS DE REFEIÇÕES → ESCOLHE → DETALHES → INGREDIENTES + SUBSTITUIÇÕES →
PASSO A PASSO → PREPARAR → REGISTRAR REFEIÇÃO.

## 24. Fluxo de primeira utilização

ONBOARDING → Preferências alimentares → Equipamentos → Forma de informar ingredientes →
Adicionar ingredientes → HOME. O onboarding deve ser curto.

## 25. O que NÃO fazer no MVP

Marketplace completo, integração real com supermercados/delivery, dashboard nutricional
complexo, contagem de calorias, sistema social, gamificação extensa, OCR perfeito,
catálogo gigantesco, planejamento semanal obrigatório.

## 26. Roadmap

- **Fase 1 — MVP:** onboarding, ingredientes por texto/áudio, cadastro de equipamentos,
  recomendação, cards com foto, tempo total/ativo, custo estimado, disponibilidade.
- **Fase 2 — Personalização:** histórico, preferências aprendidas, esforço, substituições,
  ingredientes prioritários, validade, lista de compras.
- **Fase 3 — Automação:** foto de nota fiscal, OCR, inventário automático com confirmação.
  → antecipado no MVP: foto da nota + OCR on-device + revisão antes de salvar.
- **Fase 4 — Ecossistema:** supermercados, comparação de preços, ofertas, delivery.

## 27. Métricas de sucesso

Time to Decision (≤ 60 s), Recommendation Selection Rate, Cooking Conversion, Ingredient
Utilization, Delivery displacement, Retenção.

## 28. Critérios de sucesso do MVP

Informar o que possui sem dificuldade, receber recomendações relevantes, entender as
opções, escolher sem pesquisar, preparar a refeição e usar de novo. Pergunta-chave: “Na
próxima vez que você estiver cansado e sem saber o que comer, você abriria o Brigadeiro
antes de pedir delivery?”

## 29. Princípios de produto para o desenvolvimento

Toda feature deve reduzir decisões, ser mais rápida que procurar uma receita, ajudar a
aproveitar o que já se tem, respeitar o contexto real, reduzir fricção e aumentar a
utilidade sem virar obrigação.

## 30. North Star

Transformar “o que eu vou comer?” de uma decisão cansativa em uma escolha simples. O
Brigadeiro não precisa vencer o delivery oferecendo mais comida: precisa fazer cozinhar
parecer tão fácil quanto pedir.
