package task1.services;

import task1.decorators.pizza.*;
import task1.decorators.pizza.decorated.Barbeque;
import task1.decorators.pizza.decorated.Chicken;
import task1.decorators.pizza.decorated.Mushroom;
import task1.decorators.pizza.decorated.Tomato;
import task1.delivery.WoltDelivery;
import task1.delivery.YandexDelivery;
import task1.helpers.ClockInterval;
import task1.menu.PriceRangeMenuStrategy;
import task1.menu.SeasonBasedMenuStrategy;
import task1.menu.TimeOfDayMenuStrategy;
import task1.observer.DeliverySubscriber;
import task1.observer.Table;
import task1.payment.CashStrategy;
import task1.payment.CreditCardStrategy;
import task1.restaurants.KoreanRestaurant;
import task1.restaurants.MexicanRestaurant;
import task1.restaurants.PizzeriaRestaurant;
import task1.restaurants.Restaurant;

import java.time.LocalTime;
import java.util.Optional;
import java.util.Scanner;

public class UserConsoleHandler {
    public static void handleUserChoice(Scanner sc) {
        System.out.println("----------------------------------");

        System.out.println("Choose restaurant where you want to get food!");

        System.out.println("1. Korean restaurant");
        System.out.println("2. Mexican restaurant");
        System.out.println("3. Pizzeria restaurant");


        System.out.print("Enter number of restaurant from the list: ");
        int numberOfRestaurant = sc.nextInt();
        if(numberOfRestaurant > 3 || numberOfRestaurant <= 0) throw new IllegalStateException("Error! You entered wrong number!");


        switch (numberOfRestaurant) {
            case 1 -> {
                KoreanRestaurant koreanRestaurant = new KoreanRestaurant();
                executeDefaultRestaurant(koreanRestaurant, sc);
            }
            case 2 -> {
                MexicanRestaurant mexicanRestaurant = new MexicanRestaurant();
                executeDeliverableRestaurant(mexicanRestaurant, sc);
            }
            case 3 -> {
                PizzeriaRestaurant pizzeriaRestaurant = new PizzeriaRestaurant();
                executePizzeriaRestaurant(pizzeriaRestaurant, sc);
            }
        }
    }

    private static void executePizzeriaRestaurant(PizzeriaRestaurant pizzeriaRestaurant, Scanner sc) {
        System.out.println("----------------------------------");

        int tableNumber = getTableChoiceAndReturnTableNumber(sc, pizzeriaRestaurant);

        System.out.println("Here is our pizza menu:");

        int totalPrice = executePizzaChoice(sc, 0);

        System.out.println("----------------------------------");
        System.out.println("Cooking process:");
        pizzeriaRestaurant.cookFood(tableNumber);

        System.out.println("----------------------------------");

        getPaymentChoice(pizzeriaRestaurant, sc, totalPrice);
    }

    private static int executePizzaChoice(Scanner sc, int totalCost) {
        Pizza pizza = getPizzaChoice(sc);
        pizza = getToppingsChoice(pizza, sc);

        System.out.println("----------------------------------");

        System.out.println("Your resulted pizza: " + pizza.getDescription());

        System.out.println("Do you want one more pizza? \n" +
                "1. See pizza's menu \n" +
                "2. No");

        int makeOrderAgainChoice = sc.nextInt();
        if(makeOrderAgainChoice > 2 || makeOrderAgainChoice < 0) throw new IllegalStateException("No such choice");

        if(makeOrderAgainChoice == 1) executePizzaChoice(sc, totalCost + pizza.getCost());

        return totalCost;
    }

    private static Pizza getToppingsChoice(Pizza pizza, Scanner sc) {
        System.out.println("----------------------------------");

        System.out.println("""
                Do you like to add some toppings?\s
                1. See toppings\s
                2. No""");

        System.out.print("Enter number: ");
        int isToppingAdded = sc.nextInt();
        if(isToppingAdded > 2 || isToppingAdded < 0) throw new IllegalStateException("No such choice");
        if(isToppingAdded == 2) return pizza;

        System.out.println("----------------------------------");

        System.out.println("Toppings menu:");
        System.out.println("""
                1. Barbeque sauce\s
                2. Chicken\s
                3. Mushroom\s
                4. Fresh tomatoes""");

        System.out.println("----------------------------------");
        System.out.print("Enter topping's number: ");
        int toppingChoice = sc.nextInt();
        if(toppingChoice > 4 || toppingChoice < 0) throw new IllegalStateException("No such choice");

        switch (toppingChoice){
            case 1 -> pizza = new Barbeque(pizza);
            case 2 -> pizza = new Chicken(pizza);
            case 3 -> pizza = new Mushroom(pizza);
            case 4 -> pizza = new Tomato(pizza);
        }

        return getToppingsChoice(pizza, sc);
    }

    private static Pizza getPizzaChoice(Scanner sc) {
        System.out.println("----------------------------------");
        Pizza pizza = new SimplePizza("Simple pizza");
        System.out.println("Pizza menu:");
        System.out.println("""
                1. Pepperoni\s
                2. Margaritta\s
                3. Diablo\s
                4. Pesto""");

        System.out.print("Enter pizza's number: ");
        int pizzaChoice = sc.nextInt();
        if(pizzaChoice > 4 || pizzaChoice < 0) throw new IllegalStateException("No such choice");

        switch (pizzaChoice){
            case 1 -> pizza = new Pepperoni("Pepperoni pizza");
            case 2 -> pizza = new Margaritta("Margaritta pizza");
            case 3 -> pizza = new Diablo("Diablo pizza");
            case 4 -> pizza = new Pesto("Pesto pizza");
        }

        return pizza;
    }

    private static void executeDeliverableRestaurant(MexicanRestaurant restaurant, Scanner sc) {
        Optional<DeliverySubscriber> deliverySubscriber = getDeliveryChoice(restaurant, sc);

        if(deliverySubscriber.isPresent()) {
            getMenuChoice(restaurant, sc);

            restaurant.cookFoodForDelivery();

            System.out.println("----------------------------------");

            getPaymentChoice(restaurant, sc, 0);

            System.out.println("----------------------------------");
            restaurant.deliver(deliverySubscriber.get());

        } else {
            executeDefaultRestaurant(restaurant, sc);
        }

    }

    private static Optional<DeliverySubscriber> getDeliveryChoice(MexicanRestaurant restaurant, Scanner sc) {
        System.out.println("----------------------------------");
        System.out.println("Do you want to get delivery? If yes press 1, unless press 2");
        System.out.print("Enter number: ");
        int isDelivered = sc.nextInt();
        if(isDelivered > 2 || isDelivered <= 0) throw new IllegalStateException("Error! You entered wrong number!");

        System.out.println("----------------------------------");

        if(isDelivered == 1) {
            sc.nextLine();
            System.out.println("Choose delivery:");
            System.out.println("1. Yandex food");
            System.out.println("2. Wolt");

            System.out.print("Enter number: ");
            int deliveryChoice = sc.nextInt();
            if(deliveryChoice > 2 || deliveryChoice <= 0) throw new IllegalStateException("Error! You entered wrong number!");

            switch (deliveryChoice) {
                case 1 -> restaurant.setDeliverStrategy(new YandexDelivery());
                case 2 -> restaurant.setDeliverStrategy(new WoltDelivery());
            }

            sc.nextLine();
            System.out.println();
            System.out.println("Additional information: ");
            System.out.print("Enter your name: ");
            String name = sc.nextLine();
            System.out.print("Enter your address to deliver: ");
            String address = sc.nextLine();

            DeliverySubscriber deliverySubscriber = new DeliverySubscriber(address, name);


            System.out.println("----------------------------------");
            return Optional.of(deliverySubscriber);
        }

        return Optional.empty();
    }

    private static void executeDefaultRestaurant(Restaurant restaurant, Scanner sc) {

        int tableNumber = getTableChoiceAndReturnTableNumber(sc, restaurant);

        getMenuChoice(restaurant, sc);

        System.out.println("Cooking process:");
        restaurant.cookFood(tableNumber);

        System.out.println("----------------------------------");

        getPaymentChoice(restaurant, sc, 0);
    }

    private static void getPaymentChoice(Restaurant restaurant, Scanner sc, int totalPrice) {
        System.out.println("How do you want to pay for food?");
        System.out.println("1. Cash");
        System.out.println("2. Credit card");

        System.out.print("Enter number: ");
        int paymentChoice = sc.nextInt();
        if(paymentChoice > 2 || paymentChoice <= 0) throw new IllegalStateException("Error! You entered wrong number!");

        switch (paymentChoice) {
            case 1 -> restaurant.setPaymentStrategy(new CashStrategy());
            case 2 -> restaurant.setPaymentStrategy(new CreditCardStrategy());
        }

        System.out.println("----------------------------------");

        if(totalPrice > 0) {
            System.out.println("Your bill: KZT" + totalPrice );
            restaurant.pay(totalPrice);
        } else {
            int maxPrice = 20000;
            int minPrice = 2000;
            int price = (int)(Math.random()*(maxPrice-minPrice+1)+minPrice);
            System.out.println("Your bill: KZT" + price );
            restaurant.pay(price);
        }
    }

    private static int getTableChoiceAndReturnTableNumber(Scanner sc, Restaurant restaurant) {
        System.out.println("There are 3 available tables.");

        System.out.println("1. Table 1");
        System.out.println("2. Table 2");
        System.out.println("3. Table 3");

        System.out.print("Choose one you like:");
        int tableNumber = sc.nextInt();
        if(tableNumber > 3 || tableNumber <= 0) throw new IllegalStateException("There is no such table");

        Table table = new Table(1);
        switch (tableNumber){
            case 2 -> table = new Table(2);
            case 3 -> table = new Table(3);
        }

        restaurant.addTable(table, tableNumber);

        return tableNumber;
    }

    private static void getMenuChoice(Restaurant restaurant, Scanner sc) {
        System.out.println("----------------------------------");
        System.out.println("There is price-ranged menu. Do you want to see other menus?");
        System.out.println("If yes press 1, unless press 2");

        System.out.print("Enter number: ");
        int isDeafaultMenu = sc.nextInt();
        if(isDeafaultMenu > 2 || isDeafaultMenu <= 0) throw new IllegalStateException("Error! You entered wrong number!");
        System.out.println("----------------------------------");


        ClockInterval morning = ClockInterval.between(LocalTime.of(7, 30), LocalTime.of(12, 30));
        LocalTime timeAtNow = LocalTime.now();

        boolean isTimeOfDayMenuEnabled = morning.contains(timeAtNow);

        if(isDeafaultMenu == 1) {
            System.out.println("Choose menu:");
            System.out.println("1. Season-based menu ");
            System.out.println("2. Price-ranged menu ");

            if (isTimeOfDayMenuEnabled) {
                System.out.println("3. Time-of-day menu ");
            }

            System.out.print("Enter number: ");
            int menuChoice = sc.nextInt();
            if(menuChoice <= 0) throw new IllegalStateException("Error! You entered wrong number!");
            if(isTimeOfDayMenuEnabled && menuChoice > 3) throw new IllegalStateException("Error! You entered wrong number!");
            if(!isTimeOfDayMenuEnabled && menuChoice > 2) throw new IllegalStateException("Error! You entered wrong number!");

            switch (menuChoice) {
                case 1 -> restaurant.setMenuGenerationStrategy(new SeasonBasedMenuStrategy());
                case 2 -> restaurant.setMenuGenerationStrategy(new PriceRangeMenuStrategy());
                case 3 -> restaurant.setMenuGenerationStrategy(new TimeOfDayMenuStrategy());
            }

            System.out.println("----------------------------------");
            System.out.print("Result: ");
            restaurant.generateMenu();
        }
    }
}
