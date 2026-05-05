# Duelo dos Assassinos

Jogo tático 1v1 (hotseat — mesmo teclado/rato) feito em Java + Swing.
Dois assassinos numa grelha 12x12 com paredes a bloquear movimento e linha de visão.

## Como jogar (rápido)

- 100 HP cada, 3 AP por turno.
- **Mover** (1 AP): clica numa casa adjacente livre.
- **Ataque corpo-a-corpo** (1 AP): 30 dano, casa adjacente do inimigo.
- **Shuriken** (2 AP): 20 dano, alcance 5 casas com linha de visão (paredes bloqueiam).
- **Curar** (2 AP): +30 HP, **só uma vez por jogo**.
- Casas verdes = alvos válidos para a ação selecionada.
- Botão "Terminar Turno" passa a vez.
- Vence quem reduzir o HP do oponente a 0.

## Como correr no IntelliJ

1. Abrir o IntelliJ → **New → Project** → **Java** (sem template, JDK 11+).
2. Apagar o `Main.java` que o IntelliJ cria por defeito.
3. Copiar todos os ficheiros `.java` da pasta `src/` deste projeto para a pasta `src/` do projeto novo do IntelliJ.
4. Abrir `Main.java` e clicar no botão verde de "Run" ao lado do `public static void main`.

Pronto. Sem dependências externas, só JDK.

## Ficheiros

- `Main.java` — ponto de entrada
- `GameFrame.java` — janela principal e painéis laterais
- `GamePanel.java` — desenho da grelha e cliques
- `GameState.java` — lógica do jogo
- `Assassin.java` — modelo do jogador
- `Action.java` — enum das ações
- `Tile.java` — tipo de casa (vazia / parede)

Bom duelo!
