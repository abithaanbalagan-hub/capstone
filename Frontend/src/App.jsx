import { useEffect, useState } from 'react'
import './App.css'
import Login from './Login'
import Register from './Register'


function TripPlanner({ onBack, selectedDestination = '' }) {

  const [destination, setDestination] =
    useState(selectedDestination)

  const [startDate, setStartDate] =
    useState('')

  const [endDate, setEndDate] =
    useState('')

  const [budget, setBudget] =
    useState('')

  const [travelers, setTravelers] =
    useState('1')

  const [message, setMessage] =
    useState('')

  const [error, setError] =
    useState('')

  const [loading, setLoading] =
    useState(false)


  const handleTripSubmit = async (e) => {

    e.preventDefault()

    setMessage('')
    setError('')


    if (!destination.trim()) {

      setError(
        'Please enter your destination.'
      )

      return
    }


    if (!startDate) {

      setError(
        'Please select a start date.'
      )

      return
    }


    if (!endDate) {

      setError(
        'Please select an end date.'
      )

      return
    }


    if (endDate < startDate) {

      setError(
        'End date cannot be before the start date.'
      )

      return
    }


    const start =
      new Date(startDate)

    const end =
      new Date(endDate)


    const numberOfDays =
      Math.floor(
        (end - start) /
        (1000 * 60 * 60 * 24)
      ) + 1


    if (numberOfDays > 30) {

      setError(
        'Trip duration cannot be more than 30 days.'
      )

      return
    }


    if (!budget || Number(budget) <= 0) {

      setError(
        'Budget must be greater than 0.'
      )

      return
    }


    if (!travelers || Number(travelers) < 1) {

      setError(
        'Number of travelers must be at least 1.'
      )

      return
    }


    const tripData = {

      destination:
        destination.trim(),

      startDate,

      endDate,

      budget:
        Number(budget),

      travelers:
        Number(travelers)
    }


    try {

      setLoading(true)


      const token =
        localStorage.getItem(
          'smarttripToken'
        )


      if (!token) {

        setError(
          'Login session expired. Please login again.'
        )

        return
      }


      const response =
        await fetch(
          'http://localhost:8080/api/trips',
          {
            method: 'POST',

            headers: {

              'Content-Type':
                'application/json',

              'Authorization':
                `Bearer ${token}`
            },

            body:
              JSON.stringify(tripData)
          }
        )


      if (response.status === 401) {

        localStorage.removeItem(
          'smarttripToken'
        )

        localStorage.removeItem(
          'smarttripUser'
        )

        setError(
          'Your login session has expired. Please login again.'
        )

        return
      }


      if (!response.ok) {

        const errorText =
          await response.text()


        throw new Error(
          `Trip save failed: ${response.status} ${errorText}`
        )
      }


      const savedTrip =
        await response.json()


      setMessage(
        `Trip planned successfully for ${savedTrip.destination}!`
      )


      setDestination('')
      setStartDate('')
      setEndDate('')
      setBudget('')
      setTravelers('1')


    } catch (err) {

      console.error(
        'Trip save error:',
        err
      )


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

        <h2>
          SmartTrip Planner
        </h2>


        <button onClick={onBack}>
          Back
        </button>

      </nav>


      <main className="trip-content">

        <div className="trip-card">

          <h1>
            Plan Your Trip 🇮🇳
          </h1>


          <p>
            Plan your perfect journey across India
            based on your destination, dates and budget.
          </p>


          <form
            onSubmit={handleTripSubmit}
          >

            <label>
              Destination
            </label>


            <input
              type="text"
              placeholder="Example: Munnar"
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

  const [trips, setTrips] =
    useState([])

  const [loading, setLoading] =
    useState(true)

  const [error, setError] =
    useState('')

  const [deleteLoading, setDeleteLoading] =
    useState(null)

  const [selectedTrip, setSelectedTrip] =
    useState(null)


  const fetchTrips = async () => {

    try {

      setLoading(true)
      setError('')


      const token =
        localStorage.getItem(
          'smarttripToken'
        )


      if (!token) {

        setError(
          'Please login again.'
        )

        return
      }


      const response =
        await fetch(
          'http://localhost:8080/api/trips',
          {
            method: 'GET',

            headers: {
              'Authorization':
                `Bearer ${token}`
            }
          }
        )


      if (response.status === 401) {

        localStorage.removeItem(
          'smarttripToken'
        )

        localStorage.removeItem(
          'smarttripUser'
        )

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


      setError(
        err.message
      )


    } finally {

      setLoading(false)
    }
  }


  useEffect(() => {

    fetchTrips()

  }, [])


  const handleDeleteTrip =
    async (tripId) => {

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
          localStorage.getItem(
            'smarttripToken'
          )


        if (!token) {

          setError(
            'Please login again.'
          )

          return
        }


        const response =
          await fetch(
            `http://localhost:8080/api/trips/${tripId}`,
            {
              method: 'DELETE',

              headers: {
                'Authorization':
                  `Bearer ${token}`
              }
            }
          )


        const responseText =
          await response.text()


        if (response.status === 401) {

          localStorage.removeItem(
            'smarttripToken'
          )

          localStorage.removeItem(
            'smarttripUser'
          )

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


        setTrips(
          (currentTrips) =>
            currentTrips.filter(
              (trip) =>
                trip.id !== tripId
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
              🇮🇳 {selectedTrip.destination}
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
                    🇮🇳 {trip.destination}
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
                        setSelectedTrip(
                          trip
                        )
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

                      {deleteLoading ===
                      trip.id

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

  const [
    destination,
    setDestination
  ] = useState('')


  const [
    attractions,
    setAttractions
  ] = useState([])


  const [
    searchedDestination,
    setSearchedDestination
  ] = useState('')


  const [
    loading,
    setLoading
  ] = useState(false)


  const [
    error,
    setError
  ] = useState('')


  const [
    searched,
    setSearched
  ] = useState(false)


  const searchDestination =
    async (searchValue = destination) => {

      const cleanDestination =
        searchValue.trim()


      if (!cleanDestination) {

        setError(
          'Please enter a destination.'
        )

        setAttractions([])
        setSearchedDestination('')
        setSearched(false)

        return
      }


      try {

        setLoading(true)
        setError('')
        setSearched(false)
        setAttractions([])


        const encodedDestination =
          encodeURIComponent(
            cleanDestination
          )


        const response =
          await fetch(
            `http://localhost:8080/api/destinations/search?destination=${encodedDestination}`
          )


        if (!response.ok) {

          throw new Error(
            `Search failed: ${response.status}`
          )
        }


        const data =
          await response.json()


        setSearchedDestination(
          cleanDestination
        )


        setAttractions(
          Array.isArray(data)
            ? data
            : []
        )


        setSearched(true)


      } catch (err) {

        console.error(
          'Destination search error:',
          err
        )


        setError(
          `Unable to search destination: ${err.message}`
        )


        setAttractions([])


      } finally {

        setLoading(false)
      }
    }


  const handleSearchSubmit =
    async (e) => {

      e.preventDefault()

      await searchDestination()
    }


  const handleQuickSearch =
    async (quickDestination) => {

      setDestination(
        quickDestination
      )

      await searchDestination(
        quickDestination
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
          Search Indian destinations and discover
          popular tourist attractions.
        </p>


        <form
          onSubmit={handleSearchSubmit}
          style={{
            display: 'flex',
            gap: '10px',
            marginBottom: '25px',
            flexWrap: 'wrap'
          }}
        >

          <input
            type="text"
            value={destination}
            onChange={(e) =>
              setDestination(
                e.target.value
              )
            }
            placeholder="Search destination e.g. Munnar"
            style={{
              flex: '1',
              minWidth: '250px',
              padding: '12px'
            }}
          />


          <button
            type="submit"
            disabled={loading}
          >

            {loading
              ? 'Searching...'
              : '🔍 Search'}

          </button>

        </form>


        <div
          style={{
            marginBottom: '25px'
          }}
        >

          <p>
            <strong>
              Popular searches:
            </strong>
          </p>


          <div
            style={{
              display: 'flex',
              gap: '10px',
              flexWrap: 'wrap'
            }}
          >

            {[
              'Munnar',
              'Kochi',
              'Ooty',
              'Madurai',
              'Chennai',
              'Goa'
            ].map(
              (quickDestination) => (

                <button
                  key={quickDestination}
                  type="button"
                  onClick={() =>
                    handleQuickSearch(
                      quickDestination
                    )
                  }
                  disabled={loading}
                >
                  {quickDestination}
                </button>

              )
            )}

          </div>

        </div>


        {error && (

          <p className="trip-error">
            {error}
          </p>

        )}


        {loading && (

          <p>
            🔎 Finding attractions...
          </p>

        )}


        {!loading &&
          searched &&
          attractions.length === 0 && (

            <div className="dashboard-card">

              <h2>
                No attractions found
              </h2>

              <p>
                We couldn't find tourist attractions
                for "{searchedDestination}".
                Try another Indian destination.
              </p>

            </div>

          )}


        {!loading &&
          searched &&
          attractions.length > 0 && (

            <div className="dashboard-card">

              <h2>
                📍 {searchedDestination}
              </h2>


              <p>
                ⭐ Popular tourist attractions
              </p>


              <ul>

                {attractions.map(
                  (attraction, index) => (

                    <li
                      key={`${attraction}-${index}`}
                    >
                      {attraction}
                    </li>

                  )
                )}

              </ul>


              <button
                onClick={() =>
                  onPlanDestination(
                    searchedDestination
                  )
                }
              >
                🧭 Plan This Trip
              </button>

            </div>

          )}


        {!loading &&
          !searched && (

            <div className="dashboard-card">

              <h2>
                🇮🇳 Discover India
              </h2>


              <p>
                Search for a destination above
                to discover tourist attractions.
              </p>

            </div>

          )}

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

      <nav
        className="dashboard-nav"
        style={{
          padding: '18px 5%',
          background: '#ffffff',
          borderBottom: '1px solid #e8eef5',
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center'
        }}
      >

        <div
          style={{
            display: 'flex',
            alignItems: 'center',
            gap: '12px'
          }}
        >

          <div
            style={{
              width: '52px',
              height: '52px',
              borderRadius: '16px',
              background:
                'linear-gradient(135deg, #ff9933, #138808)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              fontSize: '28px',
              boxShadow:
                '0 8px 20px rgba(19, 136, 8, 0.18)'
            }}
          >
            📍
          </div>


          <div>

            <h2
              style={{
                margin: 0,
                fontSize: '28px',
                fontWeight: '800',
                letterSpacing: '-0.5px'
              }}
            >
              SmartTrip
              <span
                style={{
                  color: '#1685c4'
                }}
              >
                Planner
              </span>
            </h2>


            <div
              style={{
                fontSize: '12px',
                letterSpacing: '4px',
                color: '#65758b',
                marginTop: '3px'
              }}
            >
              EXPLORE • PLAN • TRAVEL
            </div>

          </div>

        </div>


        <button
          onClick={onLogout}
          style={{
            padding: '14px 24px',
            border: 'none',
            borderRadius: '14px',
            background: '#eaf8fc',
            color: '#087da8',
            fontWeight: '700',
            fontSize: '16px',
            cursor: 'pointer'
          }}
        >
          ⇥ &nbsp; Logout
        </button>

      </nav>


      <main
        className="dashboard-content"
        style={{
          paddingTop: '45px'
        }}
      >

        <section
          style={{
            position: 'relative',
            overflow: 'hidden',
            borderRadius: '30px',
            padding: '45px 25px 30px',
            marginBottom: '35px',
            background:
              'linear-gradient(180deg, #f8fcff 0%, #eef9ff 100%)',
            border:
              '1px solid #dcecf4',
            boxShadow:
              '0 18px 45px rgba(20, 85, 120, 0.08)'
          }}
        >

          <div
            style={{
              position: 'absolute',
              left: '-80px',
              bottom: '-90px',
              width: '300px',
              height: '180px',
              borderRadius: '50%',
              background:
                'linear-gradient(135deg, rgba(255,153,51,0.12), rgba(255,255,255,0))'
            }}
          />


          <div
            style={{
              position: 'absolute',
              right: '-80px',
              bottom: '-90px',
              width: '300px',
              height: '180px',
              borderRadius: '50%',
              background:
                'linear-gradient(135deg, rgba(19,136,8,0.12), rgba(255,255,255,0))'
            }}
          />


          <h1
            style={{
              textAlign: 'center',
              fontSize: '58px',
              lineHeight: '1.1',
              margin: '5px 0 12px',
              fontWeight: '800',
              color: '#15233d',
              letterSpacing: '-2px'
            }}
          >
            Explore India.
            {' '}
            <span
              style={{
                color: '#138808'
              }}
            >
              Plan Smarter.
            </span>
          </h1>


          {userEmail && (

            <p
              className="dashboard-user-email"
              style={{
                textAlign: 'center',
                fontSize: '17px',
                color: '#60728a',
                margin: '0 0 10px'
              }}
            >

              Logged in as:
              {' '}

              <strong>
                {userEmail}
              </strong>

            </p>

          )}


          <p
            style={{
              textAlign: 'center',
              fontSize: '20px',
              color: '#5c7189',
              margin: '0 auto 32px',
              maxWidth: '800px'
            }}
          >
            Discover India, plan smarter and create
            unforgettable journeys.
          </p>


          <div
            style={{
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              gap: '14px',
              flexWrap: 'wrap',
              margin: '10px auto 8px'
            }}
          >

            <div
              style={{
                textAlign: 'center',
                minWidth: '105px'
              }}
            >

              <div
                style={{
                  fontSize: '46px',
                  lineHeight: '1'
                }}
              >
                🛕
              </div>

              <div
                style={{
                  fontSize: '12px',
                  fontWeight: '800',
                  letterSpacing: '2px',
                  color: '#38566d',
                  marginTop: '8px'
                }}
              >
                EXPLORE
              </div>

            </div>


            <div
              style={{
                width: '150px',
                height: '35px',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center'
              }}
            >

              <span
                style={{
                  width: '100%',
                  borderTop:
                    '3px dashed #138808'
                }}
              />

            </div>


            <div
              style={{
                textAlign: 'center',
                minWidth: '105px'
              }}
            >

              <div
                style={{
                  fontSize: '46px',
                  lineHeight: '1'
                }}
              >
                📍
              </div>

              <div
                style={{
                  fontSize: '12px',
                  fontWeight: '800',
                  letterSpacing: '2px',
                  color: '#38566d',
                  marginTop: '8px'
                }}
              >
                DISCOVER
              </div>

            </div>


            <div
              style={{
                width: '150px',
                height: '35px',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center'
              }}
            >

              <span
                style={{
                  width: '100%',
                  borderTop:
                    '3px dashed #ff9933'
                }}
              />

            </div>


            <div
              style={{
                textAlign: 'center',
                minWidth: '105px'
              }}
            >

              <div
                style={{
                  fontSize: '46px',
                  lineHeight: '1'
                }}
              >
                🏔️
              </div>

              <div
                style={{
                  fontSize: '12px',
                  fontWeight: '800',
                  letterSpacing: '2px',
                  color: '#38566d',
                  marginTop: '8px'
                }}
              >
                PLAN
              </div>

            </div>


            <div
              style={{
                width: '150px',
                height: '35px',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center'
              }}
            >

              <span
                style={{
                  width: '100%',
                  borderTop:
                    '3px dashed #138808'
                }}
              />

            </div>


            <div
              style={{
                textAlign: 'center',
                minWidth: '105px'
              }}
            >

              <div
                style={{
                  fontSize: '46px',
                  lineHeight: '1'
                }}
              >
                🌴
              </div>

              <div
                style={{
                  fontSize: '12px',
                  fontWeight: '800',
                  letterSpacing: '2px',
                  color: '#38566d',
                  marginTop: '8px'
                }}
              >
                TRAVEL
              </div>

            </div>

          </div>


          <div
            style={{
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              gap: '18px',
              marginTop: '22px'
            }}
          >

            <span
              style={{
                width: '90px',
                height: '4px',
                background: '#ff9933',
                borderRadius: '10px'
              }}
            />

            <span
              style={{
                fontSize: '15px',
                letterSpacing: '4px',
                fontWeight: '800',
                color: '#41566e'
              }}
            >
              INCREDIBLE INDIA AWAITS
            </span>

            <span
              style={{
                width: '90px',
                height: '4px',
                background: '#138808',
                borderRadius: '10px'
              }}
            />

          </div>

        </section>


        <div
          className="dashboard-cards"
          style={{
            display: 'grid',
            gridTemplateColumns:
              'repeat(3, minmax(0, 1fr))',
            gap: '28px',
            alignItems: 'stretch'
          }}
        >


          <div
            className="dashboard-card"
            style={{
              position: 'relative',
              overflow: 'hidden',
              padding: '32px',
              borderRadius: '24px',
              borderTop:
                '5px solid #ff9933',
              background: '#ffffff',
              boxShadow:
                '0 15px 35px rgba(24, 62, 90, 0.09)'
            }}
          >

            <div
              style={{
                width: '72px',
                height: '72px',
                borderRadius: '50%',
                background: '#fff0df',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                fontSize: '38px',
                marginBottom: '20px'
              }}
            >
              🗺️
            </div>


            <h2
              style={{
                fontSize: '26px',
                color: '#172b4d',
                marginBottom: '14px'
              }}
            >
              Plan a Trip
            </h2>


            <p
              style={{
                color: '#63758a',
                fontSize: '16px',
                lineHeight: '1.7',
                minHeight: '82px'
              }}
            >
              Create a personalized travel plan
              based on your destination, dates
              and budget.
            </p>


            <button
              onClick={onPlanTrip}
              style={{
                marginTop: '12px',
                padding: '14px 24px',
                border: 'none',
                borderRadius: '14px',
                background:
                  'linear-gradient(135deg, #ff9933, #f36b21)',
                color: '#ffffff',
                fontSize: '17px',
                fontWeight: '800',
                cursor: 'pointer',
                boxShadow:
                  '0 10px 22px rgba(255, 128, 30, 0.22)'
              }}
            >
              Plan a Trip&nbsp; →
            </button>


            <div
              style={{
                position: 'absolute',
                right: '-10px',
                bottom: '-20px',
                fontSize: '95px',
                opacity: 0.06
              }}
            >
              🏔️
            </div>

          </div>



          <div
            className="dashboard-card"
            style={{
              position: 'relative',
              overflow: 'hidden',
              padding: '32px',
              borderRadius: '24px',
              borderTop:
                '5px solid #1976d2',
              background: '#ffffff',
              boxShadow:
                '0 15px 35px rgba(24, 62, 90, 0.09)'
            }}
          >

            <div
              style={{
                width: '72px',
                height: '72px',
                borderRadius: '50%',
                background: '#e7f2ff',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                fontSize: '38px',
                marginBottom: '20px'
              }}
            >
              🔭
            </div>


            <h2
              style={{
                fontSize: '26px',
                color: '#172b4d',
                marginBottom: '14px'
              }}
            >
              Explore Destinations
            </h2>


            <p
              style={{
                color: '#63758a',
                fontSize: '16px',
                lineHeight: '1.7',
                minHeight: '82px'
              }}
            >
              Find Indian destinations and
              discover tourist attractions.
            </p>


            <button
              onClick={onExplore}
              style={{
                marginTop: '12px',
                padding: '14px 28px',
                border: 'none',
                borderRadius: '14px',
                background:
                  'linear-gradient(135deg, #1685c4, #2563eb)',
                color: '#ffffff',
                fontSize: '17px',
                fontWeight: '800',
                cursor: 'pointer',
                boxShadow:
                  '0 10px 22px rgba(37, 99, 235, 0.22)'
              }}
            >
              Explore&nbsp; →
            </button>


            <div
              style={{
                position: 'absolute',
                right: '-10px',
                bottom: '-20px',
                fontSize: '95px',
                opacity: 0.06
              }}
            >
              🕌
            </div>

          </div>



          <div
            className="dashboard-card"
            style={{
              position: 'relative',
              overflow: 'hidden',
              padding: '32px',
              borderRadius: '24px',
              borderTop:
                '5px solid #138808',
              background: '#ffffff',
              boxShadow:
                '0 15px 35px rgba(24, 62, 90, 0.09)'
            }}
          >

            <div
              style={{
                width: '72px',
                height: '72px',
                borderRadius: '50%',
                background: '#e9f8e8',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                fontSize: '38px',
                marginBottom: '20px'
              }}
            >
              🧳
            </div>


            <h2
              style={{
                fontSize: '26px',
                color: '#172b4d',
                marginBottom: '14px'
              }}
            >
              My Trips
            </h2>


            <p
              style={{
                color: '#63758a',
                fontSize: '16px',
                lineHeight: '1.7',
                minHeight: '82px'
              }}
            >
              View and manage your planned
              trips in one place.
            </p>


            <button
              onClick={onMyTrips}
              style={{
                marginTop: '12px',
                padding: '14px 28px',
                border: 'none',
                borderRadius: '14px',
                background:
                  'linear-gradient(135deg, #16a34a, #138808)',
                color: '#ffffff',
                fontSize: '17px',
                fontWeight: '800',
                cursor: 'pointer',
                boxShadow:
                  '0 10px 22px rgba(19, 136, 8, 0.22)'
              }}
            >
              My Trips&nbsp; →
            </button>


            <div
              style={{
                position: 'absolute',
                right: '-10px',
                bottom: '-20px',
                fontSize: '95px',
                opacity: 0.06
              }}
            >
              🌴
            </div>

          </div>

        </div>

      </main>

    </div>
  )
}



function App() {

  const [page, setPage] =
    useState('login')


  const [
    selectedDestination,
    setSelectedDestination
  ] = useState('')


  const [userEmail, setUserEmail] =
    useState('')


  useEffect(() => {

    const storedUser =
      localStorage.getItem(
        'smarttripUser'
      )


    if (!storedUser) {

      setUserEmail('')

      return
    }


    try {

      const user =
        JSON.parse(
          storedUser
        )


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

      setUserEmail(
        storedUser
      )
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

          userEmail={
            userEmail
          }


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


          onPlanDestination={
            (destination) => {

              setSelectedDestination(
                destination
              )

              setPage('trip')

            }
          }

        />

      )}

    </>

  )
}


export default App