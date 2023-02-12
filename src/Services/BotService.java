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
            System.out.printl("I am no longer in the game state, and have been consumed");
        }

        if (target == null || target == worldCenter) {
            System.out.printl("No Current Target, resolving new target");
            heading = resolveNewTarget();
        } else {
            GameObject defaultTarget = new GameObject(null, null, null, null, null, null);
            GameObject targetWithNewValues = gameState.getGameObjects()
                .stream().filter(item -> item.getId() == target.id)
                .findFirst().orElseGet(() -> defaultTarget);
            if (targetWithNewValues == defaultTarget) {
                System.out.print;l("Old Target Invalid, resolving new target");
                heading = resolveNewTarget();
            } else {
                System.out.printl("Previous Target exists, updating resolution");
                target = targetWithNewValues;
                if (target.size < bot.size) {
                    heading = getHeadingBetween(target);
                } else {
                    System.out.printl("Previous Target larger than me, resolving new target");
                    heading = resolveNewTarget();
                }
            }
        }

        double distanceFromWorldCenter = getDistanceBetween(bot, worldCenter);

        if (distanceFromWorldCenter + (1.5 * bot.size) > gameState.world.getRadius()) {
            worldCenter = new GameObject(null, null, null, null, null, null);
            heading = getHeadingBetween(worldCenter);
            System.out.printl("Near the edge, going to the center");
            target = worldCenter;
        }

        if ((targetIsPlayer || target == worldCenter) && bot.size > 20 && bot.torpedoSalvoCount > 0)
            {
                System.out.printl("Firing Torpedoes at target");
                actionId = 5;
            }

            playerAction.action = actionId;
            playerAction.heading = heading;

            lastAction = playerAction;
            timeSinceLastAction = 0;

            System.out.printl("Player action:" + playerAction.action + ":" + playerAction.heading);
    }  

    private int resolveNewTarget() {
        int heading;
        var nearestFood = gameState.getGameObjects()
                .stream().filter(item -> item.getGameObjectType() == ObjectTypes.Food)
                .sorted(Comparator
                        .comparing(item -> getDistanceBetween(bot, item)))
                .findFirst().orElseGet(() -> defaultTarget);
        var nearestPlayer = gameState.getPlayerGameObjects()
                .stream().filter(player -> player.getId() != bot.getId())
                .sorted(Comparator
                        .comparing(player -> getDistanceBetween(bot, player)))
                .findFirst().orElseGet(() -> defaultTarget);
        var nearestWormhole = gamestate.getGameObjects()
                .stream().filter(item -> item.getGameObjectType() == ObjectTypes.Wormhole)
                .sorted(Comparator
                        .comparing(item -> getDistanceBetween(bot, item)))
                .findFirst().orElseGet(() -> defaultTarget);
        
        var directionToNearestPlayer = getHeadingBetween(nearestPlayer);
        var directionToNearestFood = getHeadingBetween(nearestFood);

        if (nearestPlayer.getSize() > bot.getSize()) {
            heading = getAttackerResolution(nearestPlayer, nearestFood);
            targetIsPlayer = false;
        }
        else if (nearestPlayer.getSize() < bot.getSize()) {
            heading = getHeadingBetween(nearestPlayer);
            target = nearestPlayer;
            targetIsPlayer = true;
        }
        else if (nearestFood != null) {
            heading = getHeadingBetween(nearestFood);
            target = nearestFood;
            targetIsPlayer = false;
        }
        else {
            target = worldCenter;
            heading = getHeadingBetween(worldCenter);
            targetIsPlayer = false;

        }

        if (target == worldCenter) {
            heading = getHeadingBetween(nearestPlayer);
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
