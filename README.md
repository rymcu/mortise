<pre align="center">

███╗   ███╗  ██████╗  ██████╗  ████████╗ ██╗ ███████╗ ███████╗
████╗ ████║ ██╔═══██╗ ██╔══██╗ ╚══██╔══╝ ██║ ██╔════╝ ██╔════╝
██╔████╔██║ ██║   ██║ ██████╔╝    ██║    ██║ ███████╗ █████╗  
██║╚██╔╝██║ ██║   ██║ ██╔══██╗    ██║    ██║ ╚════██║ ██╔══╝  
██║ ╚═╝ ██║ ╚██████╔╝ ██║  ██║    ██║    ██║ ███████║ ███████╗
╚═╝     ╚═╝  ╚═════╝  ╚═╝  ╚═╝    ╚═╝    ╚═╝ ╚══════╝ ╚══════╝


Built by RYMCU
</pre>

## Introduction

一款现代化的后台管理脚手架项目，使用 Spring Boot 3 + Spring Security + JWT + Mybatis Plus + Redis 实现

## Features

- [x] 用户管理
- [x] 角色管理
- [x] 菜单管理
- [x] 权限管理
- [ ] 字典管理
- [ ] 事件日志
- [ ] 对象存储

## Requirements

- Eclipse Temurin 21
- MySQL 5.8 / PostgreSQL 17
- Maven 3.6.0+

## Local Development

1. Clone the repository
2. Run `update_hosts.bat` or `update_hosts.sh` to update the hosts file
3. Install [mkcert](https://github.com/FiloSottile/mkcert#installation) , generate a self-signed certificate for `rymcu.local` and `*.rymcu.local`
4. Run `mkcert -install && mkcert -key-file key.pem -cert-file cert.pem rymcu.local *.rymcu.local`
5. optional: Run `mkcert -install && mkcert -key-file key.pem -cert-file cert.pem npm.rymcu.local`
6. Run `compose.yaml` with Docker Compose
7. Open `http://localhost:81` in your browser, and you should see the Nginx Proxy Manager UI, default username and password are `admin@example.com` and `changeme`
8. add `rymcu.local` and `*.rymcu.local` to the `Trusted Domains` field in the Nginx Proxy Manager UI
9. add `npm.rymcu.local`: `app:81`
10. add `auth.rymcu.local`: `logto:3010`
11. add `logto.rymcu.local`: `logto:3011`
12. add `rymcu.local`: `app:80`

More information about Nginx Proxy Manager can be found [here](https://nginxproxymanager.com/guide/)


## Licenses

[MIT](./LICENSE)


## Contributors
[![](https://contrib.rocks/image?repo=rymcu/mortise&max=1000)](https://github.com/rymcu/mortise/graphs/contributors)
