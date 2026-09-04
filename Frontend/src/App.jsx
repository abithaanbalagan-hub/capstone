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


  const handleTripSubmit = (e) => {

    e.preventDefault()

    setMessage('')
    setError('')


    // Destination validation
    if (!destination.trim()) {
      setError('Please enter your destination.')
      return
    }


    // Start date validation
    if (!startDate) {
      setError('Please select a start date.')
      return
    }


    // End date validation
    if (!endDate) {
      setError('Please select an end date.')
      return
    }


    // Date validation
    if (endDate < startDate) {
      setError('End date cannot be before the start date.')
      return
    }


    // Budget validation
    if (!budget || Number(budget) <= 0) {
      setError('Budget must be greater than 0.')
      return
    }


    // Travelers validation
    if (!travelers || Number(travelers) < 1) {
      setError('Number of travelers must be at least 1.')
      return
    }


    // Success
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

            {/* Destination */}

            <label>
              Destination
            </label>

            <input
              type="text"
              placeholder="Example: Paris, France"
              value={destination}
              onChange={(e) => setDestination(e.target.value)}
            />


            {/* Start Date */}

            <label>
              Start Date
            </label>

            <input
              type="date"
              value={startDate}
              onChange={(e) => setStartDate(e.target.value)}
            />


            {/* End Date */}

            <label>
              End Date
            </label>

            <input
              type="date"
              value={endDate}
              onChange={(e) => setEndDate(e.target.value)}
            />


            {/* Budget */}

            <label>
              Budget
            </label>

            <input
              type="number"
              placeholder="Enter your budget"
              value={budget}
              onChange={(e) => setBudget(e.target.value)}
              min="1"
            />


            {/* Travelers */}

            <label>
              Number of Travelers
            </label>

            <input
              type="number"
              value={travelers}
              onChange={(e) => setTravelers(e.target.value)}
              min="1"
            />


            <button type="submit">
              Generate Trip Plan
            </button>

          </form>


          {/* Error message */}

          {error && (
            <p className="trip-error">
              {error}
            </p>
          )}


          {/* Success message */}

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

        <h2>
          SmartTrip Planner
        </h2>

        <button onClick={onLogout}>
          Logout
        </button>

      </nav>


      <main className="dashboard-content">

        <h1>
          Plan Your Next Adventure ✈️
        </h1>

        <p>
          Discover places, plan trips and create unforgettable journeys.
        </p>


        <div className="dashboard-cards">


          {/* Plan Trip */}

          <div className="dashboard-card">

            <h2>
              🌍 Plan a Trip
            </h2>

            <p>
              Create a personalized travel plan based on your destination,
              dates and budget.
            </p>

            <button onClick={onPlanTrip}>
              Plan a Trip
            </button>

          </div>


          {/* Explore */}

          <div className="dashboard-card">

            <h2>
              🗺️ Explore Destinations
            </h2>

            <p>
              Find interesting destinations and discover new places to visit.
            </p>

            <button>
              Explore
            </button>

          </div>


          {/* My Trips */}

          <div className="dashboard-card">

            <h2>
              🎒 My Trips
            </h2>

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