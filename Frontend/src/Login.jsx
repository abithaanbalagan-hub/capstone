import { useState } from 'react'

function Login({ onRegister, onLoginSuccess }) {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [message, setMessage] = useState('')

  const handleLogin = async (e) => {
    e.preventDefault()
    setMessage('Logging in...')

    try {
      const response = await fetch(
        'http://localhost:8080/api/users/login',
        {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({
            email: email,
            password: password,
          }),
        }
      )

      if (response.ok) {
        const user = await response.json()

        console.log('Logged in user:', user)

        setMessage('Login successful! Welcome back.')

        setEmail('')
        setPassword('')

        onLoginSuccess()
      } else {
        const errorText = await response.text()

        console.error('Login failed:', errorText)

        setMessage('Invalid email or password.')
      }
    } catch (error) {
      console.error('LOGIN ERROR:', error)

      setMessage(
        `Backend connection failed: ${error.message}`
      )
    }
  }

  return (
    <div className="login-page">

      {/* Decorative travel elements */}
      <div className="login-decoration login-circle-one"></div>
      <div className="login-decoration login-circle-two"></div>

      <div className="login-container">

        {/* Left travel section */}
        <div className="login-hero">

          <div className="hero-content">

            <div className="brand-badge">
              ✈️ SmartTrip
            </div>

            <h1>
              Your next
              <br />
              adventure
              <br />
              <span>starts here.</span>
            </h1>

            <p>
              Plan unforgettable journeys, discover amazing
              destinations and keep all your trips in one place.
            </p>

            <div className="travel-features">

              <div className="travel-feature">
                <span>🌍</span>
                <div>
                  <strong>Explore</strong>
                  <small>Discover new destinations</small>
                </div>
              </div>

              <div className="travel-feature">
                <span>🗺️</span>
                <div>
                  <strong>Plan</strong>
                  <small>Create your perfect itinerary</small>
                </div>
              </div>

              <div className="travel-feature">
                <span>🎒</span>
                <div>
                  <strong>Travel</strong>
                  <small>Make memories that last</small>
                </div>
              </div>

            </div>

          </div>

        </div>

        {/* Login section */}
        <div className="login-section">

          <div className="login-card">

            <div className="login-icon">
              ✈️
            </div>

            <h2>Welcome Back, Traveller!</h2>

            <p className="login-subtitle">
              Sign in to continue your journey
            </p>

            <form onSubmit={handleLogin}>

              <label>Email Address</label>

              <input
                type="email"
                placeholder="you@example.com"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
              />

              <label>Password</label>

              <input
                type="password"
                placeholder="Enter your password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
              />

              <button
                type="submit"
                className="login-button"
              >
                Start Your Journey
                <span>→</span>
              </button>

            </form>

            {message && (
              <p className="login-message">
                {message}
              </p>
            )}

            <div className="login-divider">
              <span></span>
              <small>or</small>
              <span></span>
            </div>

            <p className="register-text">
              New to SmartTrip?{' '}
              <span onClick={onRegister}>
                Create an account
              </span>
            </p>

          </div>

        </div>

      </div>

    </div>
  )
}

export default Login