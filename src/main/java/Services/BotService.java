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
    private boolean targetIsPlayer = false;

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
        PlayerActions actionID = PlayerActions.Forward;
        var heading = new Random().nextInt(360);
        playerAction.heading = heading;

        List<GameObject> playerGameObjects;
        playerGameObjects = gameState.getPlayerGameObjects();

        if (!playerGameObjects.contains(this.bot)) {
            System.out.println("I am no longer in the game state, and have been consumed");
        }

        if (target == null || target == worldCenter) {
            System.out.println("No Current Target, resolving new target");
            heading = resolveNewTarget();
        } else {
            GameObject defaultTarget = target;
            GameObject targetWithNewValues = gameState.getGameObjects()
                .stream().filter(item -> item.getId() == target.getId())
                .findFirst().orElseGet(() -> defaultTarget);
            if (targetWithNewValues == defaultTarget) {
                System.out.println("Old Target Invalid, resolving new target");
                heading = resolveNewTarget();
            } else {
                System.out.println("Previous Target exists, updating resolution");
                target = targetWithNewValues;
                if (target.size < bot.size) {
                    heading = getHeadingBetween(target);
                } else {
                    System.out.println("Previous Target larger than me, resolving new target");
                    heading = resolveNewTarget();
                }
            }
        }

        double distanceFromWorldCenter = getDistanceBetween(bot, worldCenter);

        if (distanceFromWorldCenter + (1.5 * bot.size) > gameState.world.getRadius()) {
            heading = getHeadingBetween(worldCenter);
            System.out.println("Near the edge, going to the center");
            target = worldCenter;
        }

        if ((targetIsPlayer || target == worldCenter) && bot.size > 20 && bot.torpedoSalvoCount > 0)
            {
                System.out.println("Firing Torpedoes at target");
                actionID = PlayerActions.FireTorpedoes;
            }

        playerAction.action = actionID;
        playerAction.heading = heading;

        lastAction = playerAction;
        timeSinceLastAction = 0;

        System.out.println("Player action:" + playerAction.action + ":" + playerAction.heading);

        this.playerAction = playerAction;
    }  

    private int resolveNewTarget() {
        var heading = new Random().nextInt(360);
        List<GameObject> nearestFood = gameState.getGameObjects()
                .stream().filter(item -> item.getGameObjectType() == ObjectTypes.Food)
                .sorted(Comparator
                        .comparing(item -> getDistanceBetween(bot, item)))
                .collect(Collectors.toList());
        List<GameObject> nearestPlayer = gameState.getPlayerGameObjects()
                .stream().filter(player -> player.getId() != bot.getId())
                .sorted(Comparator
                        .comparing(player -> getDistanceBetween(bot, player)))
                .collect(Collectors.toList());
        
        System.out.println("test");

        if ((nearestPlayer.get(0)).size > bot.size) {
            heading = getAttackerResolution(nearestPlayer.get(0), nearestFood.get(0));
            targetIsPlayer = false;
        }
        else if ((nearestPlayer.get(0)).size < bot.size) {
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
        System.out.println("test3");
        int direction = toDegrees(Math.atan2(otherObject.getPosition().y - bot.getPosition().y,
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
