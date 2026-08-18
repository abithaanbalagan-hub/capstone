import './App.css'

function App() {
  return (
    <div className="app">

      {/* Navbar */}
      <nav className="navbar">
        <div className="logo">
          SmartTrip
        </div>

        <div className="nav-links">
          <a href="#home">Home</a>
          <a href="#hotels">Hotels</a>
          <a href="#planner">Trip Planner</a>
          <a href="#about">About</a>
          <button className="login-btn">Login</button>
        </div>
      </nav>

      {/* Hero Section */}
      <section className="hero-section" id="home">

        <div className="hero-content">
          <p className="tagline">TRAVEL SMART • SPEND SMART</p>

          <h1>
            Plan Your Perfect Trip
            <span> Within Your Budget</span>
          </h1>

          <p className="description">
            Find the right hotel, manage your travel budget,
            discover amazing places and create your day-wise
            trip itinerary in one place.
          </p>

          <div className="hero-buttons">
            <button className="primary-btn">
              Start Planning
            </button>

            <button className="secondary-btn">
              Explore Hotels
            </button>
          </div>
        </div>

      </section>

      {/* Features */}
      <section className="features" id="about">

        <h2>Everything You Need for Your Trip</h2>

        <p className="section-text">
          Plan, book and manage your complete trip from one platform.
        </p>

        <div className="feature-container">

          <div className="feature-card">
            <div className="icon">🏨</div>
            <h3>Smart Hotel Search</h3>
            <p>
              Search and filter hotels based on price,
              rating, location and amenities.
            </p>
          </div>

          <div className="feature-card">
            <div className="icon">💰</div>
            <h3>Budget Planner</h3>
            <p>
              Estimate hotel, food, travel and activity
              expenses based on your budget.
            </p>
          </div>

          <div className="feature-card">
            <div className="icon">🗓️</div>
            <h3>Smart Itinerary</h3>
            <p>
              Generate a day-wise trip plan with
              suitable tourist places and expenses.
            </p>
          </div>

        </div>
      </section>

      {/* Innovation Section */}
      <section className="innovation" id="planner">

        <div>
          <p className="tagline">OUR SMART FEATURE</p>

          <h2>
            Stay Within Your
            <span> Budget</span>
          </h2>

          <p>
            If your estimated trip cost exceeds your budget,
            SmartTrip can suggest lower-cost hotels and
            activities to help you plan a more affordable trip.
          </p>

          <button className="primary-btn">
            Plan My Trip
          </button>
        </div>

        <div className="budget-box">
          <h3>Sample Trip Budget</h3>

          <div className="budget-row">
            <span>Hotel</span>
            <strong>₹6,000</strong>
          </div>

          <div className="budget-row">
            <span>Food</span>
            <strong>₹3,000</strong>
          </div>

          <div className="budget-row">
            <span>Travel</span>
            <strong>₹2,000</strong>
          </div>

          <div className="budget-row">
            <span>Activities</span>
            <strong>₹2,000</strong>
          </div>

          <hr />

          <div className="budget-total">
            <span>Total</span>
            <strong>₹13,000</strong>
          </div>

          <p className="remaining">
            ✓ Within your ₹15,000 budget
          </p>
        </div>

      </section>

      {/* Footer */}
      <footer>
        <h3>SmartTrip</h3>
        <p>
          Smart Hotel Booking and Budget Trip Planner
        </p>
        <p>© 2026 SmartTrip. All rights reserved.</p>
      </footer>

    </div>
  )
}

export default App