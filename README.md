# Javos

> Sistema de gestão open-source em Java, inspirado no projeto [MAP-OS](https://github.com/RamonSilva20/mapos).

[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)

Javos é um sistema de gestão desenvolvido em Java, inspirado no projeto open-source MAP-OS (originalmente escrito em PHP). O objetivo do Javos é oferecer uma alternativa moderna e robusta para quem deseja utilizar a mesma lógica e funcionalidades do MAP-OS, mas em um ambiente baseado na linguagem Java, com frontend moderno em React.

## Início Rápido

```bash
# Com Docker Compose
cd deploy
docker-compose up --build
```

Acesse [http://localhost:8080](http://localhost:8080) — usuário: `admin` / senha: `admin123`

## Documentação

Consulte a documentação completa em [`/docs/README.md`](docs/README.md).

## Estrutura do Projeto

| Pasta       | Descrição                                      |
|-------------|------------------------------------------------|
| `/backend`  | API REST em Spring Boot (Java 17)              |
| `/frontend` | Interface web em React + TypeScript            |
| `/docs`     | Documentação do projeto                        |
| `/deploy`   | Dockerfile, docker-compose e scripts de build  |

## Licença

Este projeto é licenciado sob a **GNU General Public License v3.0**.
Veja o arquivo [LICENSE](LICENSE) para detalhes.
