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
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const handleTripSubmit = async (e) => {
    e.preventDefault()

    setMessage('')
    setError('')

    if (!destination.trim()) {
      setError('Please enter your destination.')
      return
    }

    if (!startDate) {
      setError('Please select a start date.')
      return
    }

    if (!endDate) {
      setError('Please select an end date.')
      return
    }

    if (endDate < startDate) {
      setError('End date cannot be before the start date.')
      return
    }

    if (!budget || Number(budget) <= 0) {
      setError('Budget must be greater than 0.')
      return
    }

    if (!travelers || Number(travelers) < 1) {
      setError('Number of travelers must be at least 1.')
      return
    }

    const tripData = {
      destination: destination.trim(),
      startDate: startDate,
      endDate: endDate,
      budget: Number(budget),
      travelers: Number(travelers)
    }

    try {
      setLoading(true)

      const response = await fetch('http://localhost:8080/api/trips', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(tripData)
      })

      if (!response.ok) {
        throw new Error('Failed to save trip')
      }

      const savedTrip = await response.json()

      setMessage(
        `Trip planned successfully for ${savedTrip.destination}!`
      )

      setDestination('')
      setStartDate('')
      setEndDate('')
      setBudget('')
      setTravelers('1')
    } catch (err) {
      console.error('Trip save error:', err)
      setError(
        'Backend connection failed. Please make sure the backend is running.'
      )
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="trip-page">
      <nav className="dashboard-nav">
        <h2>SmartTrip Planner</h2>
        <button onClick={onBack}>Back to Dashboard</button>
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
            />

            <label>Start Date</label>

            <input
              type="date"
              value={startDate}
              onChange={(e) => setStartDate(e.target.value)}
            />

            <label>End Date</label>

            <input
              type="date"
              value={endDate}
              onChange={(e) => setEndDate(e.target.value)}
            />

            <label>Budget</label>

            <input
              type="number"
              placeholder="Enter your budget"
              value={budget}
              onChange={(e) => setBudget(e.target.value)}
              min="1"
            />

            <label>Number of Travelers</label>

            <input
              type="number"
              value={travelers}
              onChange={(e) => setTravelers(e.target.value)}
              min="1"
            />

            <button type="submit" disabled={loading}>
              {loading ? 'Saving Trip...' : 'Generate Trip Plan'}
            </button>
          </form>

          {error && (
            <p className="trip-error">
              {error}
            </p>
          )}

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
              Create a personalized travel plan based on your
              destination, dates and budget.
            </p>

            <button onClick={onPlanTrip}>
              Plan a Trip
            </button>
          </div>

          <div className="dashboard-card">
            <h2>🗺️ Explore Destinations</h2>

            <p>
              Find interesting destinations and discover new places
              to visit.
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