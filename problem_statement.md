# Problem Statement

## 1. Title

Smart Hotel Booking and Budget Trip Planner

## 2. Domain

Travel and Tourism / Web Application

## 3. Who is the user? (2–3 user types, with roles)

### 1. Traveler

- Register and login to the application.
- Search hotels based on destination and travel dates.
- Enter total travel budget, number of days, and number of travelers.
- View budget-friendly hotel recommendations.
- Generate a personalized trip plan and itinerary.
- Book hotels and view booking history.
- View estimated trip expenses.

### 2. Hotel Owner

- Register and login as a hotel owner.
- Add and manage hotel details.
- Add rooms and update room availability.
- Set room prices and amenities.
- View customer bookings.
- View customer reviews.

### 3. Admin

- Manage travelers and hotel owners.
- Approve and manage hotel listings.
- Monitor hotel bookings.
- Remove invalid or inappropriate hotel listings.
- View overall system information and reports.

## 4. What problem are we solving?

Planning a trip within a fixed budget is difficult because travelers need to search for hotels, estimate expenses, select tourist places, and prepare an itinerary using different applications. Users may also choose a hotel without knowing whether the remaining budget is sufficient for food, local transportation, and tourist activities.

This project solves the problem by providing a single platform for hotel booking and budget-based trip planning. The system allows travelers to enter their total budget and travel duration and calculates an estimated trip cost.

The main innovation is a budget-based smart planning feature that recommends suitable hotels and generates a day-wise itinerary while trying to keep the complete trip within the user's budget. If the estimated cost exceeds the budget, the system can suggest lower-cost alternatives such as a cheaper hotel or a different combination of activities.

## 5. Proposed Solution (what the application will do, feature-wise)

The application will provide the following features:

### User Management
- User registration and login.
- Role-based access for Traveler, Hotel Owner, and Admin.

### Smart Hotel Search
- Search hotels by destination.
- Filter hotels by price, rating, and amenities.
- Display room availability and pricing.

### Hotel Booking
- Select available rooms.
- Book hotel rooms.
- View booking details and booking history.

### Budget-Based Trip Planner
- User enters destination, total budget, number of days, and number of travelers.
- System estimates hotel, food, local travel, and tourist activity expenses.
- System compares the estimated cost with the user's total budget.

### Smart Budget Optimization
- If the planned trip exceeds the user's budget, the system suggests lower-cost alternatives.
- The system recommends hotels and activities that better fit the available budget.
- Display remaining budget after estimated expenses.

### Automatic Itinerary Generation
- Generate a day-wise trip itinerary based on the selected destination and number of days.
- Recommend tourist places based on the available budget.
- Display estimated expenses for the planned activities.

### Tourist Place Recommendations
- Store tourist attractions with location, estimated cost, and suggested visit duration.
- Recommend suitable attractions for the selected trip.

### Reviews
- Travelers can submit reviews and ratings for hotels after their stay.
- Hotel owners can view customer reviews.

### Admin Dashboard
- Manage users, hotels, bookings, and tourist places.
- Approve or remove hotel listings.
- Monitor system activities.

## 6. Core Entities / Database Tables

1. Users
2. Hotels
3. Rooms
4. Bookings
5. TripPlans
6. TouristPlaces
7. Payments
8. Reviews

## 7. User Roles & Permissions

### Admin

- Manage users and hotel owners.
- Approve, update, or remove hotel listings.
- Manage tourist place information.
- Monitor bookings.
- View system reports.

### Traveler

- Register and login.
- Search and filter hotels.
- Enter trip budget and travel details.
- Generate a budget-based trip plan.
- View estimated expenses and itinerary.
- Book hotel rooms.
- View booking history.
- Submit hotel reviews and ratings.

### Hotel Owner

- Register and login.
- Add and update hotel details.
- Add and manage rooms.
- Update room availability and pricing.
- View customer bookings.
- View customer reviews.

## 8. Success Criteria

- A traveler should be able to register and log in successfully.
- A traveler should be able to search and filter hotels by destination, price, rating, and amenities.
- A traveler should be able to book an available hotel room successfully.
- A traveler should be able to enter a total trip budget, number of days, and number of travelers.
- The system should calculate the estimated total trip expenses.
- The system should generate a day-wise itinerary based on the selected destination and trip duration.
- The system should recommend suitable hotels and tourist activities based on the user's budget.
- If the estimated trip cost exceeds the budget, the system should suggest lower-cost alternatives.
- Travelers should be able to view their itinerary and booking history.
- Hotel owners should be able to manage hotels, rooms, availability, and bookings.
- Admin should be able to manage users, hotels, tourist places, and bookings.

## 9. Out of Scope

The following features will NOT be included in this version:

- Flight booking
- Train or bus ticket booking
- Live GPS navigation
- Online travel insurance
- AI chatbot
- Real-time traffic information
- Multi-language support
- Real-time hotel price synchronization with external booking platforms
- Real-time payment gateway integration
- Live weather-based itinerary changes

## 10. Chosen Track

Java (Spring Boot)