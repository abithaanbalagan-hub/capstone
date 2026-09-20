import { useEffect, useState } from 'react'
import './App.css'
import Login from './Login'
import Register from './Register'

function TripPlanner({ onBack, selectedDestination = '' }) {
  const [destination, setDestination] = useState(selectedDestination)
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

    const start = new Date(startDate)
    const end = new Date(endDate)

    const numberOfDays =
      Math.floor(
        (end - start) / (1000 * 60 * 60 * 24)
      ) + 1

    if (numberOfDays > 30) {
      setError('Trip duration cannot be more than 30 days.')
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
      startDate,
      endDate,
      budget: Number(budget),
      travelers: Number(travelers)
    }

    try {
      setLoading(true)

      const token =
        localStorage.getItem('smarttripToken')

      if (!token) {
        setError(
          'Login session expired. Please login again.'
        )
        return
      }

      const response = await fetch(
        'http://localhost:8080/api/trips',
        {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
          },
          body: JSON.stringify(tripData)
        }
      )

      if (response.status === 401) {
        localStorage.removeItem('smarttripToken')
        localStorage.removeItem('smarttripUser')

        setError(
          'Your login session has expired. Please login again.'
        )
        return
      }

      if (!response.ok) {
        const errorText = await response.text()

        throw new Error(
          `Trip save failed: ${response.status} ${errorText}`
        )
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
        `Backend error: ${err.message}`
      )

    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="trip-page">

      <nav className="dashboard-nav">
        <h2>SmartTrip Planner</h2>

        <button onClick={onBack}>
          Back
        </button>
      </nav>

      <main className="trip-content">

        <div className="trip-card">

          <h1>Plan Your Trip ✈️</h1>

          <p>
            Enter your trip details to start planning
            your adventure.
          </p>

          <form onSubmit={handleTripSubmit}>

            <label>
              Destination
            </label>

            <input
              type="text"
              placeholder="Example: Paris, France"
              value={destination}
              onChange={(e) =>
                setDestination(e.target.value)
              }
            />

            <label>
              Start Date
            </label>

            <input
              type="date"
              value={startDate}
              onChange={(e) =>
                setStartDate(e.target.value)
              }
            />

            <label>
              End Date
            </label>

            <input
              type="date"
              value={endDate}
              onChange={(e) =>
                setEndDate(e.target.value)
              }
            />

            <label>
              Budget
            </label>

            <input
              type="number"
              placeholder="Enter your budget"
              value={budget}
              onChange={(e) =>
                setBudget(e.target.value)
              }
              min="1"
            />

            <label>
              Number of Travelers
            </label>

            <input
              type="number"
              value={travelers}
              onChange={(e) =>
                setTravelers(e.target.value)
              }
              min="1"
            />

            <button
              type="submit"
              disabled={loading}
            >
              {loading
                ? 'Saving Trip...'
                : 'Generate Trip Plan'}
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


function MyTrips({ onBack }) {

  const [trips, setTrips] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [deleteLoading, setDeleteLoading] = useState(null)
  const [selectedTrip, setSelectedTrip] = useState(null)

  const fetchTrips = async () => {

    try {

      setLoading(true)
      setError('')

      const token =
        localStorage.getItem('smarttripToken')

      if (!token) {
        setError('Please login again.')
        return
      }

      const response = await fetch(
        'http://localhost:8080/api/trips',
        {
          method: 'GET',
          headers: {
            'Authorization': `Bearer ${token}`
          }
        }
      )

      if (response.status === 401) {

        localStorage.removeItem('smarttripToken')
        localStorage.removeItem('smarttripUser')

        throw new Error(
          'Your login session has expired.'
        )
      }

      if (!response.ok) {

        const errorText =
          await response.text()

        throw new Error(
          `Failed to fetch trips: ${response.status} ${errorText}`
        )
      }

      const data =
        await response.json()

      setTrips(data)

    } catch (err) {

      console.error(
        'My Trips error:',
        err
      )

      setError(err.message)

    } finally {

      setLoading(false)

    }
  }


  useEffect(() => {
    fetchTrips()
  }, [])


  const handleDeleteTrip = async (tripId) => {

    const confirmed =
      window.confirm(
        'Are you sure you want to delete this trip?'
      )

    if (!confirmed) {
      return
    }

    try {

      setDeleteLoading(tripId)
      setError('')

      const token =
        localStorage.getItem('smarttripToken')

      if (!token) {
        setError('Please login again.')
        return
      }

      const response = await fetch(
        `http://localhost:8080/api/trips/${tripId}`,
        {
          method: 'DELETE',
          headers: {
            'Authorization': `Bearer ${token}`
          }
        }
      )

      const responseText =
        await response.text()

      if (response.status === 401) {

        localStorage.removeItem('smarttripToken')
        localStorage.removeItem('smarttripUser')

        setError(
          'Your login session has expired. Please login again.'
        )

        return
      }

      if (response.status === 404) {

        setError(
          'Trip not found or this trip does not belong to the logged-in user.'
        )

        return
      }

      if (!response.ok) {

        setError(
          `Delete failed: ${response.status} ${responseText}`
        )

        return
      }

      setTrips((currentTrips) =>
        currentTrips.filter(
          (trip) => trip.id !== tripId
        )
      )

      if (
        selectedTrip &&
        selectedTrip.id === tripId
      ) {
        setSelectedTrip(null)
      }

    } catch (err) {

      console.error(
        'Delete trip error:',
        err
      )

      setError(
        `Delete error: ${err.message}`
      )

    } finally {

      setDeleteLoading(null)

    }
  }


  /*
   * View Trip page
   */
  if (selectedTrip) {

    return (
      <div className="dashboard-page">

        <nav className="dashboard-nav">

          <h2>
            SmartTrip Planner
          </h2>

          <button
            onClick={() =>
              setSelectedTrip(null)
            }
          >
            Back to My Trips
          </button>

        </nav>

        <main className="dashboard-content">

          <div className="dashboard-card">

            <h1>
              ✈️ {selectedTrip.destination}
            </h1>

            <p>
              <strong>
                Start Date:
              </strong>{' '}
              {selectedTrip.startDate}
            </p>

            <p>
              <strong>
                End Date:
              </strong>{' '}
              {selectedTrip.endDate}
            </p>

            <p>
              <strong>
                Budget:
              </strong>{' '}
              ₹{selectedTrip.budget}
            </p>

            <p>
              <strong>
                Travelers:
              </strong>{' '}
              {selectedTrip.travelers}
            </p>

            {selectedTrip.tripPlan && (

              <div className="trip-plan">

                <h3>
                  🗓️ Trip Plan
                </h3>

                <pre>
                  {selectedTrip.tripPlan}
                </pre>

              </div>

            )}

            <button
              onClick={() =>
                handleDeleteTrip(
                  selectedTrip.id
                )
              }
              disabled={
                deleteLoading ===
                selectedTrip.id
              }
            >
              {deleteLoading ===
              selectedTrip.id
                ? 'Deleting...'
                : '🗑️ Delete Trip'}
            </button>

          </div>

        </main>

      </div>
    )
  }


  /*
   * My Trips list page
   */
  return (
    <div className="dashboard-page">

      <nav className="dashboard-nav">

        <h2>
          SmartTrip Planner
        </h2>

        <button onClick={onBack}>
          Back to Dashboard
        </button>

      </nav>

      <main className="dashboard-content">

        <h1>
          🎒 My Trips
        </h1>

        <p>
          View your planned trips in one place.
        </p>

        {loading && (

          <p>
            Loading your trips...
          </p>

        )}

        {error && (

          <p className="trip-error">
            {error}
          </p>

        )}

        {!loading &&
          !error &&
          trips.length === 0 && (

            <p>
              You haven't planned any trips yet.
            </p>

          )}

        {!loading &&
          !error &&
          trips.length > 0 && (

            <div className="dashboard-cards">

              {trips.map((trip) => (

                <div
                  className="dashboard-card"
                  key={trip.id}
                >

                  <h2>
                    ✈️ {trip.destination}
                  </h2>

                  <p>
                    <strong>
                      Start Date:
                    </strong>{' '}
                    {trip.startDate}
                  </p>

                  <p>
                    <strong>
                      End Date:
                    </strong>{' '}
                    {trip.endDate}
                  </p>

                  <p>
                    <strong>
                      Budget:
                    </strong>{' '}
                    ₹{trip.budget}
                  </p>

                  <p>
                    <strong>
                      Travelers:
                    </strong>{' '}
                    {trip.travelers}
                  </p>

                  <div
                    style={{
                      display: 'flex',
                      gap: '10px',
                      marginTop: '15px'
                    }}
                  >

                    <button
                      onClick={() =>
                        setSelectedTrip(trip)
                      }
                    >
                      👁️ View Trip
                    </button>

                    <button
                      onClick={() =>
                        handleDeleteTrip(
                          trip.id
                        )
                      }
                      disabled={
                        deleteLoading ===
                        trip.id
                      }
                    >
                      {deleteLoading === trip.id
                        ? 'Deleting...'
                        : '🗑️ Delete Trip'}
                    </button>

                  </div>

                </div>

              ))}

            </div>

          )}

      </main>

    </div>
  )
}


function ExploreDestinations({
  onBack,
  onPlanDestination
}) {

  const [selectedDestination, setSelectedDestination] =
    useState(null)

  const destinations = [
    {
      name: 'Paris, France',
      description:
        'Explore the Eiffel Tower, museums and beautiful streets.',
      attractions: [
        'Eiffel Tower',
        'Louvre Museum',
        'Arc de Triomphe',
        'Seine River'
      ]
    },

    {
      name: 'Tokyo, Japan',
      description:
        'Discover modern city life, temples and Japanese culture.',
      attractions: [
        'Tokyo Tower',
        'Shibuya Crossing',
        'Senso-ji Temple',
        'Tokyo Skytree'
      ]
    },

    {
      name: 'Dubai, UAE',
      description:
        'Enjoy modern attractions, shopping and desert adventures.',
      attractions: [
        'Burj Khalifa',
        'Dubai Mall',
        'Palm Jumeirah',
        'Dubai Marina'
      ]
    },

    {
      name: 'Rome, Italy',
      description:
        'Experience ancient history, famous landmarks and Italian food.',
      attractions: [
        'Colosseum',
        'Roman Forum',
        'Trevi Fountain',
        'Pantheon'
      ]
    }
  ]


  if (selectedDestination) {

    return (
      <div className="dashboard-page">

        <nav className="dashboard-nav">

          <h2>
            SmartTrip Planner
          </h2>

          <button
            onClick={() =>
              setSelectedDestination(null)
            }
          >
            Back to Destinations
          </button>

        </nav>

        <main className="dashboard-content">

          <h1>
            📍 {selectedDestination.name}
          </h1>

          <p>
            {selectedDestination.description}
          </p>

          <div className="dashboard-card">

            <h2>
              ⭐ Popular Attractions
            </h2>

            <ul>

              {selectedDestination.attractions.map(
                (attraction) => (

                  <li key={attraction}>
                    {attraction}
                  </li>

                )
              )}

            </ul>

            <button
              onClick={() =>
                onPlanDestination(
                  selectedDestination.name
                )
              }
            >
              ✈️ Plan This Trip
            </button>

          </div>

        </main>

      </div>
    )
  }


  return (
    <div className="dashboard-page">

      <nav className="dashboard-nav">

        <h2>
          SmartTrip Planner
        </h2>

        <button onClick={onBack}>
          Back to Dashboard
        </button>

      </nav>

      <main className="dashboard-content">

        <h1>
          🗺️ Explore Destinations
        </h1>

        <p>
          Discover interesting places for your
          next adventure.
        </p>

        <div className="dashboard-cards">

          {destinations.map((destination) => (

            <div
              className="dashboard-card"
              key={destination.name}
            >

              <h2>
                📍 {destination.name}
              </h2>

              <p>
                {destination.description}
              </p>

              <button
                onClick={() =>
                  setSelectedDestination(
                    destination
                  )
                }
              >
                Explore
              </button>

            </div>

          ))}

        </div>

      </main>

    </div>
  )
}


function Dashboard({
  userEmail,
  onLogout,
  onPlanTrip,
  onMyTrips,
  onExplore
}) {

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

        {userEmail && (
          <p className="dashboard-user-email">
            Logged in as: <strong>{userEmail}</strong>
          </p>
        )}

        <p>
          Discover places, plan trips and create
          unforgettable journeys.
        </p>

        <div className="dashboard-cards">

          <div className="dashboard-card">

            <h2>
              🌍 Plan a Trip
            </h2>

            <p>
              Create a personalized travel plan
              based on your destination, dates
              and budget.
            </p>

            <button onClick={onPlanTrip}>
              Plan a Trip
            </button>

          </div>


          <div className="dashboard-card">

            <h2>
              🗺️ Explore Destinations
            </h2>

            <p>
              Find interesting destinations and
              discover new places to visit.
            </p>

            <button onClick={onExplore}>
              Explore
            </button>

          </div>


          <div className="dashboard-card">

            <h2>
              🎒 My Trips
            </h2>

            <p>
              View and manage your planned trips
              in one place.
            </p>

            <button onClick={onMyTrips}>
              My Trips
            </button>

          </div>

        </div>

      </main>

    </div>
  )
}


function App() {

  const [page, setPage] =
    useState('login')

  const [selectedDestination, setSelectedDestination] =
    useState('')

  const [userEmail, setUserEmail] = useState('')

  useEffect(() => {
    const storedUser = localStorage.getItem('smarttripUser')

    if (!storedUser) {
      setUserEmail('')
      return
    }

    try {
      const user = JSON.parse(storedUser)

      if (typeof user === 'string') {
        setUserEmail(user)
      } else {
        setUserEmail(
          user?.email ||
          user?.userEmail ||
          ''
        )
      }
    } catch {
      setUserEmail(storedUser)
    }
  }, [page])


  return (
    <>

      {page === 'login' && (

        <Login
          onRegister={() =>
            setPage('register')
          }

          onLoginSuccess={() =>
            setPage('dashboard')
          }
        />

      )}


      {page === 'register' && (

        <Register
          onLogin={() =>
            setPage('login')
          }
        />

      )}


      {page === 'dashboard' && (

        <Dashboard
          userEmail={userEmail}

          onLogout={() => {

            localStorage.removeItem(
              'smarttripToken'
            )

            localStorage.removeItem(
              'smarttripUser'
            )

            setPage('login')
          }}

          onPlanTrip={() => {

            setSelectedDestination('')
            setPage('trip')

          }}

          onMyTrips={() =>
            setPage('myTrips')
          }

          onExplore={() =>
            setPage('explore')
          }

        />

      )}


      {page === 'trip' && (

        <TripPlanner

          selectedDestination={
            selectedDestination
          }

          onBack={() =>
            setPage('dashboard')
          }

        />

      )}


      {page === 'myTrips' && (

        <MyTrips

          onBack={() =>
            setPage('dashboard')
          }

        />

      )}


      {page === 'explore' && (

        <ExploreDestinations

          onBack={() =>
            setPage('dashboard')
          }

          onPlanDestination={(destination) => {

            setSelectedDestination(
              destination
            )

            setPage('trip')

          }}

        />

      )}

    </>
  )
}

export default App