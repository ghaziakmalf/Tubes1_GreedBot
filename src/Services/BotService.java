package Services;

import Enums.*;
import Models.*;

import java.util.*;
import java.util.stream.*;

public class BotService {
    private GameObject bot;
    private GameState gameState;
    private GameObject target;
    private GameObject worldCenter;
    private PlayerAction playerAction;
    private PlayerAction lastAction;
    private int timeSinceLastAction;
    private bool targetIsPlayer = false;

    public BotService() {
        this.playerAction = new PlayerAction();
        this.gameState = new GameState();
    }

    public GameObject getBot() {
        return this.bot;
    }

    public void setBot(GameObject bot) {
        this.bot = bot;
    }

    public PlayerAction getPlayerAction() {
        return this.playerAction;
    }

    public void setPlayerAction(PlayerAction playerAction) {
        this.playerAction = playerAction;
    }

    public void computeNextPlayerAction(PlayerAction playerAction) {
        var actionId = 1;
        var heading = 90;

        List<GameObject> playerGameObjects;
        playerGameObjects = gameState.getPlayerGameObjects();
        GameObject target;
        GameObject worldCenter;

        if (!playerGameObjects.contains(bot)) {
            System.out.print("I am no longer in the game state, and have been consumed");
        }

        if (target == null || target == worldCenter) {
            System.out.print("No Current Target, resolving new target");
            heading = ResolveNewTarget();
        } else {
            GameObject defaultTarget = new GameObject(null, null, null, null, null, null);
            GameObject targetWithNewValues = gameState.getGameObjects()
                .stream().filter(item -> item.getId() == target.id)
                .findFirst().orElseGet(() -> defaultTarget);
            if (targetWithNewValues == defaultTarget) {
                System.out.print("Old Target Invalid, resolving new target");
                heading = ResolveNewTarget();
            } else {
                System.out.print("Previous Target exists, updating resolution");
                target = targetWithNewValues;
                if (target.size < bot.size) {
                    heading = GetDirection(bot, target);
                } else {
                    System.out.print("Previous Target larger than me, resolving new target");
                    heading = ResolveNewTarget();
                }
            }
        }

        double distanceFromWorldCenter = GetDistanceBetween(worldCenter);

        if (distanceFromWorldCenter + (1.5 * bot.size) > gameState.world.getRadius()) {
            worldCenter = new GameObject(null, null, null, null, null, null);
            heading = GetDirection(bot, worldCenter);
            System.out.print("Near the edge, going to the center");
            target = worldCenter;
        }

        if ((targetIsPlayer || target == worldCenter) && bot.size > 20 && bot.torpedoSalvoCount > 0)
            {
                System.out.print("Firing Torpedoes at target");
                actionId = 5;
            }

            playerAction.action = actionId;
            playerAction.heading = heading;

            lastAction = playerAction;
            timeSinceLastAction = 0;

            playerAction = playerAction;

            System.out.print("Player action:" + playerAction.action + ":" + playerAction.heading);
    }  

    private int resolveNewTarger() {
        int heading;
        var nearestFood = gameState.getGameObjects()
                .stream().filter(item -> item.getGameObjectType() == ObjectTypes.Food)
                .sorted(Comparator
                        .comparing(item -> getDistanceBetween(bot, item)))
                .collect(Collectors.toList());
        var nearestPlayer = gameState.getPlayerGameObjects()
                .stream().filter(player -> player.getId() != bot.getId())
                .sorted(Comparator
                        .comparing(player -> getDistanceBetween(bot, player)))
                .collect(Coolectors.toList());
        var nearestWormhole = gamestate.getGameObjects()
                .stream().filter(item -> item.getGameObjectType() == ObjectTypes.Wormhole)
                .sorted(Comparator
                        .comparing(item -> getDistanceBetween(bot, item)))
                .collect(Collectors2.toList());
        
        var directionToNearestPlayer = getHeadingBetween(nearestPlayer.get(0));
        var directionToNearestFood = getHeadingBetween(nearestFood.get(0));

        if ((nearestPlayer.get(0)).getSize() > bot.getSize()) {
            heading = getAttackerResolution(nearestPlayer.get(0), nearestFood.get(0));
            targetIsPlayer = false;
        }
        else if ((nearestPlayer.get(0)).getSize() < bot.getSize()) {
            heading = getHeadingBetween(nearestPlayer.get(0));
            target = nearestPlayer.get(0);
            targetIsPlayer = true;
        }
        else if (nearestFood != null) {
            heading = getHeadingBetween(nearestFood.get(0));
            target = nearestFood.get(0);
            targetIsPlayer = false;
        }
        else {
            target = worldCenter;
            heading = getHeadingBetween(worldCenter);
            targetIsPlayer = false;

        }

        if (target == worldCenter) {
            heading = getHeadingBetween(nearestPlayer.get(0));
        }

        return heading;
    }

    private int getAttackerResolution(GameObject attacker, GameObject closestFood) {
        if (closestFood == null) {
            return getOppositeHeading(attacker);
        }

        var distanceToAttacker = getDistanceBetween(bot, attacker);
        var distanceBetweenAttackerAndFood = getDistanceBetween(attacker, closestFood);

        if ((distanceToAttacker > attacker.speed) && (distanceBetweenAttackerAndFood > distanceToAttacker)) {
            return getHeadingBetween(closestFood);
        }
        else {
            return getOppositeHeading(attacker);
        }
    }

    public GameState getGameState() {
        return this.gameState;
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
        updateSelfState();
    }

    private void updateSelfState() {
        Optional<GameObject> optionalBot = gameState.getPlayerGameObjects().stream().filter(gameObject -> gameObject.id.equals(bot.id)).findAny();
        optionalBot.ifPresent(bot -> this.bot = bot);
    }

    private double getDistanceBetween(GameObject object1, GameObject object2) {
        var triangleX = Math.abs(object1.getPosition().x - object2.getPosition().x);
        var triangleY = Math.abs(object1.getPosition().y - object2.getPosition().y);
        return Math.sqrt(triangleX * triangleX + triangleY * triangleY);
    }

    private int getHeadingBetween(GameObject otherObject) {
        var direction = toDegrees(Math.atan2(otherObject.getPosition().y - bot.getPosition().y,
                otherObject.getPosition().x - bot.getPosition().x));
        return (direction + 360) % 360;
    }

    private int getOppositeHeading(GameObject otherObject) {
        return toDegrees(Math.atan2(otherObject.getPosition().y - bot.getPosition().y,
        otherObject.getPosition().x - bot.getPosition().x));
    }

    private int toDegrees(double v) {
        return (int) (v * (180 / Math.PI));
    }
}
