# 🎯 AzBounty

AzBounty is a Minecraft plugin for version 1.20–1.21.4 that adds a player bounty system with GUI, Vault economy support, and rich configuration.

## ✨ Features

- 🎯 Set bounties on players
- 📋 View active bounties via GUI
- 💰 Rewards paid using Vault economy
- 📜 Track completed bounties
- 🧾 Customizable config and messages
- 📦 Lightweight and optimized

## 🧰 Requirements

- Minecraft Server (Spigot or Paper) 1.20–1.21.4
- [Vault](https://www.spigotmc.org/resources/vault.34315/)
- Economy plugin (e.g., EssentialsX Economy)

## 🛠 Commands

| Command            | Description                    | Permission        |
|--------------------|--------------------------------|-------------------|
| `/bounty`          | Open the main bounty menu      | `azbounty.use`    |

## 🔐 Permissions

- `azbounty.use` – Allow access to bounty GUI and command
- `azbounty.admin` – (Optional) For admin features if implemented

## 📂 Configuration

- `config.yml` – General settings
- `messages.yml` – Customizable language/messages

## 💡 How It Works

1. Players open the bounty menu with `/bounty`
2. Choose a target player and place a bounty using in-game currency
3. If another player kills the target, they receive the reward
4. All bounty history is tracked and viewable via GUI

## 🧱 File Structure

