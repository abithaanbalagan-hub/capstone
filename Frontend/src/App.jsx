import './App.css'

function App() {
  return (
    <div className="app">
      <nav className="navbar">
        <h2>Smart Trip Planner</h2>

        <div className="nav-links">
          <a href="#">Home</a>
          <a href="#">Explore</a>
          <a href="#">My Trips</a>
          <button>Login</button>
        </div>
      </nav>

      <main className="hero-section">
        <div className="hero-content">
          <p className="welcome">WELCOME TO SMART TRIP PLANNER</p>

          <h1>
            Plan Your Trip.
            <br />
            Create Memories.
          </h1>

          <p className="description">
            Discover amazing destinations, create personalized trip plans,
            and make your travel experience simple and memorable.
          </p>

          <div className="hero-buttons">
            <button className="primary-btn">Plan My Trip</button>
            <button className="secondary-btn">Explore Destinations</button>
          </div>
        </div>
      </main>

      <section className="features">
        <div className="feature-card">
          <h3>🌍 Explore</h3>
          <p>Find beautiful destinations for your next adventure.</p>
        </div>

        <div className="feature-card">
          <h3>🗺️ Plan</h3>
          <p>Create a trip plan based on your budget and preferences.</p>
        </div>

        <div className="feature-card">
          <h3>✈️ Travel</h3>
          <p>Enjoy your journey with an organized travel plan.</p>
        </div>
      </section>
    </div>
  )
}

export default App