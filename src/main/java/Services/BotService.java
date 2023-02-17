package Services;

import Enums.*;
import Models.*;

import java.util.*;
import java.util.stream.*;

public class BotService {
    private GameObject bot;
    private GameState gameState;
    private GameObject target;
    private GameObject nearestOpponent;
    private GameObject teleporter;
    private GameObject supernovaPickup;
    private GameObject supernovaBomb;
    private GameObject worldCenter;
    private PlayerAction playerAction;
    private boolean avoidingPlayer = false;
    private boolean targetIsPlayer = false;
    private boolean afterburnerCondition = false;
    private boolean teleporterCondition = false;
    private boolean supernovaCondition = false;

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
        playerAction.heading = resolveNewTarget();
        playerAction.action = resolveNewAction();

        System.out.println("Player action:" + playerAction.action + ":" + playerAction.heading);

        this.playerAction = playerAction;
    }  

    private PlayerActions resolveNewAction() {
        if (!afterburnerCondition && targetIsPlayer && (getDistanceBetween(target, bot) < 2*bot.size) && ((bot.size - target.size) >= 20))
            {
                System.out.println("Activating afterburner");
                afterburnerCondition = true;
                return PlayerActions.StartAfterburner;
            }
        else if (afterburnerCondition && (((getDistanceBetween(target, bot) > 2*bot.size)) || (!targetIsPlayer) || ((bot.size - target.size) < 20)))
            {
                System.out.println("Deactivating afterburner");
                afterburnerCondition = false;
                return PlayerActions.StopAfterburner;
            }
        else if (!supernovaCondition && targetIsPlayer && (bot.supernovaAvailable > 0))
            {
                System.out.println("Firing Supernova");
                supernovaCondition = true;
                return PlayerActions.FireSupernova;
            }
        else if (supernovaCondition && targetIsPlayer && supernovaBomb != null && (getDistanceBetween(target, supernovaBomb) < 200))
            {
                System.out.println("Detonating Supernova");
                supernovaCondition = false;
                return PlayerActions.DetonateSupernova;
            }
        else if (!teleporterCondition && targetIsPlayer && bot.teleporterCount > 0 && ((bot.size - target.size) > 30) && (getDistanceBetween(target, bot) > 500))
            {
                System.out.println("Firing Teleporter");
                teleporterCondition = true;
                return PlayerActions.FireTeleport;
            }
        else if (teleporterCondition && targetIsPlayer && teleporter != null && (getDistanceBetween(target, teleporter) < 2*nearestOpponent.size))
            {
                System.out.println("Teleporting");
                teleporterCondition = false;
                return PlayerActions.Teleport;
            }
        else if (targetIsPlayer && bot.size > 20 && bot.torpedoSalvoCount > 0 && (getDistanceBetween(target, bot) < 200))
            {
                System.out.println("Firing Torpedoes at target");
                return PlayerActions.FireTorpedoes;
            }
        else if (avoidingPlayer && (bot.shieldCount > 0) && (getDistanceBetween(nearestOpponent, bot) < 3*nearestOpponent.size) && (bot.size > 50) && (nearestOpponent.torpedoSalvoCount > 0))
            {
                System.out.println("Activating shield");
                return PlayerActions.ActivateShield;
            }
        else 
            {
                return PlayerActions.Forward;
            }
    }

    private int resolveNewTarget() {
        var heading = new Random().nextInt(360);
        if (!gameState.getGameObjects().isEmpty()) {
            var nearestFood = gameState.getGameObjects()
                    .stream().filter(item -> item.getGameObjectType() == ObjectTypes.Food)
                    .sorted(Comparator
                            .comparing(item -> getDistanceBetween(bot, item)))
                    .collect(Collectors.toList());
            var nearestSuperFood = gameState.getGameObjects()
                    .stream().filter(item -> item.getGameObjectType() == ObjectTypes.Superfood)
                    .sorted(Comparator
                            .comparing(item -> getDistanceBetween(bot, item)))
                    .collect(Collectors.toList());
            var nearestGasCloud = gameState.getGameObjects()
                    .stream().filter(item -> item.getGameObjectType() == ObjectTypes.GasCloud)
                    .sorted(Comparator
                            .comparing(item -> getDistanceBetween(bot, item)))
                    .collect(Collectors.toList());
            var nearestPlayer = gameState.getPlayerGameObjects()
                    .stream().filter(player -> player.getId() != bot.getId())
                    .sorted(Comparator
                            .comparing(player -> getDistanceBetween(bot, player)))
                    .collect(Collectors.toList());

            if (nearestPlayer.size() > 0) {
                nearestOpponent = nearestPlayer.get(0);
            }

            if (teleporterCondition) {
                var firedTeleporter = gameState.getGameObjects()
                        .stream().filter(item -> item.getGameObjectType() == ObjectTypes.Teleporter)
                        .sorted(Comparator
                                .comparing(item -> getDistanceBetween(nearestOpponent, item)))
                        .collect(Collectors.toList());

                if (firedTeleporter.size() > 0) {
                    teleporter = firedTeleporter.get(0);
                }
            }

            var availableSupernova = gameState.getGameObjects()
                    .stream().filter(item -> item.getGameObjectType() == ObjectTypes.SupernovaPickup)
                    .sorted(Comparator
                            .comparing(item -> getDistanceBetween(bot, item)))
                    .collect(Collectors.toList());

            if (availableSupernova.size() > 0) {
                supernovaPickup = availableSupernova.get(0);
            }

            var bombSupernova = gameState.getGameObjects()
                    .stream().filter(item -> item.getGameObjectType() == ObjectTypes.SupernovaBomb)
                    .sorted(Comparator
                            .comparing(item -> getDistanceBetween(bot, item)))
                    .collect(Collectors.toList());

            if (bombSupernova.size() > 0) {
                supernovaBomb = availableSupernova.get(0);
            }

            if (bot.size > 50 && bot.torpedoSalvoCount > 0) {
                heading = getHeadingBetween(nearestPlayer.get(0));
                target = nearestPlayer.get(0);
                avoidingPlayer = false;
                targetIsPlayer = true;
            }
            else if ((nearestPlayer.get(0)).size > bot.size && (getDistanceBetween(nearestPlayer.get(0), bot) < 200)) {
                heading = getAttackerResolution(nearestPlayer.get(0), nearestSuperFood.get(0), nearestFood.get(0));
                avoidingPlayer = true;
                targetIsPlayer = false;
                System.out.println("Avoiding Opponent");
            }
            else if ((getDistanceBetween(nearestGasCloud.get(0), bot)) < 3*bot.size) {
                heading = getHeadingBetween(nearestGasCloud.get(0)) + 90;
                avoidingPlayer = false;
                targetIsPlayer = false;
                System.out.println("Avoiding Gas Cloud");
            }
            else if ((nearestPlayer.get(0)).size < bot.size && (getDistanceBetween(nearestPlayer.get(0), bot) < 200)) {
                heading = getHeadingBetween(nearestPlayer.get(0));
                target = nearestPlayer.get(0);
                avoidingPlayer = false;
                targetIsPlayer = true;
                System.out.println("Targeting Opponent");
            }
            else if ((supernovaPickup != null) && (getDistanceBetween(supernovaPickup, bot) < 250)) {
                heading = getHeadingBetween(supernovaPickup);
                target = supernovaPickup;
                avoidingPlayer = false;
                targetIsPlayer = false;
                System.out.println("Heading for Supernova");
            }
            else if (nearestSuperFood != null) {
                if (nearestFood != null) {

                    var distanceToSuperFood = getDistanceBetween(bot, nearestSuperFood.get(0));
                    var distanceToFood = getDistanceBetween(bot, nearestFood.get(0));
                    
                    if (distanceToSuperFood > distanceToFood) {
                        heading = getHeadingBetween(nearestFood.get(0));
                        target = nearestFood.get(0);
                        avoidingPlayer = false;
                        targetIsPlayer = false;
                        System.out.println("Targeting Food");
                    }
                    else {
                        heading = getHeadingBetween(nearestSuperFood.get(0));
                        target = nearestSuperFood.get(0);
                        avoidingPlayer = false;
                        targetIsPlayer = false;
                        System.out.println("Targeting Superfood"); 
                    }
                }
                else {
                    heading = getHeadingBetween(nearestSuperFood.get(0));
                    target = nearestSuperFood.get(0);
                    avoidingPlayer = false;
                    targetIsPlayer = false;
                    System.out.println("Targeting Superfood");  
                }
            }
            else if (nearestFood != null) {
                heading = getHeadingBetween(nearestFood.get(0));
                target = nearestFood.get(0);
                avoidingPlayer = false;
                targetIsPlayer = false;
                System.out.println("Targeting Food");
            }
            
            Position centerPosition = new Position();
            var distanceFromWorldCenter = getDistanceBetween(bot,new GameObject(null, null, null, null, centerPosition, null, null, null, null, null, null));
    
            World world = gameState.getWorld();
            Integer radius = world.getRadius();
    
            if (radius == null) {
                if (distanceFromWorldCenter + (1.5 * bot.size) > 1000) {
                    worldCenter = new GameObject(null, null, null, null, centerPosition, null, null, null, null, null, null);
                    heading = getHeadingBetween(worldCenter);
                    target = worldCenter;
                    targetIsPlayer = false;
                    System.out.println("Near the edge, going to the center");
                }
            }
            else {
                if (distanceFromWorldCenter + (1.5 * bot.size) > radius) {
                    worldCenter = new GameObject(null, null, null, null, centerPosition, null, null, null, null, null, null);
                    heading = getHeadingBetween(worldCenter);
                    target = worldCenter;
                    targetIsPlayer = false;
                    System.out.println("Near the edge, going to the center");
                }
            }
        }

        return heading;
    }

    private int getAttackerResolution(GameObject attacker, GameObject closestSuperFood, GameObject closestFood) {
        if (closestFood == null && closestSuperFood == null) {
            return getOppositeHeading(attacker);
        } 

        var distanceToAttacker = getDistanceBetween(bot, attacker);
        var distanceToSuperFood = getDistanceBetween(bot, closestSuperFood);
        var distanceToFood = getDistanceBetween(bot, closestFood);
        var distanceBetweenAttackerAndSuperFood = getDistanceBetween(attacker, closestSuperFood);
        var distanceBetweenAttackerAndFood = getDistanceBetween(attacker, closestFood);

        if ((distanceToAttacker > attacker.speed) && ((distanceBetweenAttackerAndSuperFood - distanceToAttacker) > 2*attacker.size)) {
            if (distanceToFood > distanceToSuperFood) {
                return getHeadingBetween(closestSuperFood);
            } 
            else {
                return getHeadingBetween(closestFood);
            }
        }
        else if ((distanceToAttacker > attacker.speed) && ((distanceBetweenAttackerAndFood - distanceToAttacker) > 2*attacker.size)) {
            if (distanceToSuperFood > distanceToFood) {
                return getHeadingBetween(closestFood);
            } 
            else {
                return getHeadingBetween(closestSuperFood);
            }
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
        int direction = toDegrees(Math.atan2(otherObject.getPosition().y - bot.getPosition().y,
                otherObject.getPosition().x - bot.getPosition().x));
        return (direction + 360) % 360;
    }

    private int getOppositeHeading(GameObject otherObject) {
        return (getHeadingBetween(otherObject) + 180) % 360;
    }

    private int toDegrees(double v) {
        return (int) (v * (180 / Math.PI));
    }
}
