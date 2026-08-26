import { useState } from 'react'

import './App.css'

import Login from './Login'
import Register from './Register'

function TripPlanner({ onBack }) {
  const [destination, setDestination] = useState('')
  const [startDate, setStartDate] = useState('')
  const [endDate, setEndDate] = useState('')
  const [budget, setBudget] = useState('')
  const [travelers, setTravelers] = useState('1')
  const [message, setMessage] = useState('')

  const handleTripSubmit = (e) => {
    e.preventDefault()

    setMessage(
      `Trip planned successfully for ${destination}!`
    )
  }

  return (
    <div className="trip-page">
      <nav className="dashboard-nav">
        <h2>SmartTrip Planner</h2>

        <button onClick={onBack}>
          Back to Dashboard
        </button>
      </nav>

      <main className="trip-content">
        <div className="trip-card">
          <h1>Plan Your Trip ✈️</h1>

          <p>
            Enter your trip details to start planning your adventure.
          </p>

          <form onSubmit={handleTripSubmit}>
            <label>Destination</label>

            <input
              type="text"
              placeholder="Example: Paris, France"
              value={destination}
              onChange={(e) => setDestination(e.target.value)}
              required
            />

            <label>Start Date</label>

            <input
              type="date"
              value={startDate}
              onChange={(e) => setStartDate(e.target.value)}
              required
            />

            <label>End Date</label>

            <input
              type="date"
              value={endDate}
              onChange={(e) => setEndDate(e.target.value)}
              required
            />

            <label>Budget</label>

            <input
              type="number"
              placeholder="Enter your budget"
              value={budget}
              onChange={(e) => setBudget(e.target.value)}
              min="1"
              required
            />

            <label>Number of Travelers</label>

            <input
              type="number"
              value={travelers}
              onChange={(e) => setTravelers(e.target.value)}
              min="1"
              required
            />

            <button type="submit">
              Generate Trip Plan
            </button>
          </form>

          {message && (
            <p className="trip-success">
              {message}
            </p>
          )}
        </div>
      </main>
    </div>
  )
}

function Dashboard({ onLogout, onPlanTrip }) {
  return (
    <div className="dashboard-page">
      <nav className="dashboard-nav">
        <h2>SmartTrip Planner</h2>

        <button onClick={onLogout}>
          Logout
        </button>
      </nav>

      <main className="dashboard-content">
        <h1>Plan Your Next Adventure ✈️</h1>

        <p>
          Discover places, plan trips and create unforgettable journeys.
        </p>

        <div className="dashboard-cards">

          <div className="dashboard-card">
            <h2>🌍 Plan a Trip</h2>

            <p>
              Create a personalized travel plan based on your destination,
              dates and budget.
            </p>

            <button onClick={onPlanTrip}>
              Plan a Trip
            </button>
          </div>

          <div className="dashboard-card">
            <h2>🗺️ Explore Destinations</h2>

            <p>
              Find interesting destinations and discover new places to visit.
            </p>

            <button>
              Explore
            </button>
          </div>

          <div className="dashboard-card">
            <h2>🎒 My Trips</h2>

            <p>
              View and manage your planned trips in one place.
            </p>

            <button>
              My Trips
            </button>
          </div>

        </div>
      </main>
    </div>
  )
}

function App() {
  const [page, setPage] = useState('login')

  return (
    <>
      {page === 'login' && (
        <Login
          onRegister={() => setPage('register')}
          onLoginSuccess={() => setPage('dashboard')}
        />
      )}

      {page === 'register' && (
        <Register
          onLogin={() => setPage('login')}
        />
      )}

      {page === 'dashboard' && (
        <Dashboard
          onLogout={() => setPage('login')}
          onPlanTrip={() => setPage('trip')}
        />
      )}

      {page === 'trip' && (
        <TripPlanner
          onBack={() => setPage('dashboard')}
        />
      )}
    </>
  )
}

export default App