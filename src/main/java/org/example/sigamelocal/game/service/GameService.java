package org.example.sigamelocal.game.service;

import org.example.sigamelocal.game.model.Player;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class GameService {

    private final List<Player> players = new ArrayList<>();

    public Player addPlayer(String name) {
        Player player = new Player(
                name,
                UUID.randomUUID().toString()
        );

        players.add(player);
        return player;
    }
    public List<Player> getPlayers() {
        return players;
    }
}
