import { useState } from 'react'

function Login({ onRegister, onLoginSuccess }) {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [message, setMessage] = useState('')

  const [showForgotPassword, setShowForgotPassword] = useState(false)
  const [forgotEmail, setForgotEmail] = useState('')
  const [otp, setOtp] = useState('')
  const [newPassword, setNewPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [forgotStep, setForgotStep] = useState(1)
  const [forgotMessage, setForgotMessage] = useState('')
  const [loading, setLoading] = useState(false)

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
        const result = await response.json()

        console.log('Login response:', result)

        // Save JWT token
        localStorage.setItem('smarttripToken', result.token)

        // Save user details
        localStorage.setItem(
          'smarttripUser',
          JSON.stringify(result.user)
        )

        setMessage('Login successful! Welcome back.')
        setEmail('')
        setPassword('')

        onLoginSuccess()
      } else {
        const errorMessage = await response.text()
        console.error('LOGIN FAILED:', errorMessage)
        setMessage('Invalid email or password.')
      }
    } catch (error) {
      console.error('LOGIN ERROR:', error)
      setMessage(`Backend connection failed: ${error.message}`)
    }
  }

  const handleForgotPassword = () => {
    setShowForgotPassword(true)
    setForgotStep(1)
    setForgotMessage('')
    setForgotEmail('')
    setOtp('')
    setNewPassword('')
    setConfirmPassword('')
  }

  const handleSendOtp = async (e) => {
    e.preventDefault()

    if (!forgotEmail.trim()) {
      setForgotMessage('Please enter your email address.')
      return
    }

    setLoading(true)
    setForgotMessage('Sending OTP...')

    try {
      const response = await fetch(
        'http://localhost:8080/api/users/forgot-password',
        {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({
            email: forgotEmail,
          }),
        }
      )

      const result = await response.text()

      if (response.ok) {
        setForgotMessage('OTP sent successfully! Check your email.')
        setForgotStep(2)
      } else {
        setForgotMessage(result || 'Email not found.')
      }
    } catch (error) {
      console.error('FORGOT PASSWORD ERROR:', error)
      setForgotMessage(
        `Backend connection failed: ${error.message}`
      )
    } finally {
      setLoading(false)
    }
  }

  const handleVerifyOtp = async (e) => {
    e.preventDefault()

    if (!otp.trim()) {
      setForgotMessage('Please enter the OTP.')
      return
    }

    setLoading(true)
    setForgotMessage('Verifying OTP...')

    try {
      const response = await fetch(
        'http://localhost:8080/api/users/verify-otp',
        {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({
            email: forgotEmail,
            otp: otp,
          }),
        }
      )

      const result = await response.text()

      if (response.ok) {
        setForgotMessage('OTP verified successfully!')
        setForgotStep(3)
      } else {
        setForgotMessage(result || 'Invalid or expired OTP.')
      }
    } catch (error) {
      console.error('OTP VERIFY ERROR:', error)
      setForgotMessage(
        `Backend connection failed: ${error.message}`
      )
    } finally {
      setLoading(false)
    }
  }

  const handleResetPassword = async (e) => {
    e.preventDefault()

    if (newPassword.length < 6) {
      setForgotMessage(
        'Password must be at least 6 characters.'
      )
      return
    }

    if (newPassword !== confirmPassword) {
      setForgotMessage('Passwords do not match.')
      return
    }

    setLoading(true)
    setForgotMessage('Resetting password...')

    try {
      const response = await fetch(
        'http://localhost:8080/api/users/reset-password',
        {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({
            email: forgotEmail,
            otp: otp,
            newPassword: newPassword,
          }),
        }
      )

      const result = await response.text()

      if (response.ok) {
        setForgotMessage(
          'Password reset successfully! You can now login.'
        )

        setTimeout(() => {
          setShowForgotPassword(false)
          setForgotStep(1)
          setForgotEmail('')
          setOtp('')
          setNewPassword('')
          setConfirmPassword('')
          setForgotMessage('')
        }, 2000)
      } else {
        setForgotMessage(
          result || 'Password reset failed.'
        )
      }
    } catch (error) {
      console.error('RESET PASSWORD ERROR:', error)
      setForgotMessage(
        `Backend connection failed: ${error.message}`
      )
    } finally {
      setLoading(false)
    }
  }

  const closeForgotPassword = () => {
    setShowForgotPassword(false)
    setForgotStep(1)
    setForgotEmail('')
    setOtp('')
    setNewPassword('')
    setConfirmPassword('')
    setForgotMessage('')
  }

  return (
    <div className="login-page">

      <div className="login-decoration login-circle-one"></div>
      <div className="login-decoration login-circle-two"></div>

      <div className="login-container">

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

        <div className="login-section">

          <div className="login-card">

            <div className="login-icon">
              ✈️
            </div>

            <h2>
              Welcome Back, Traveller!
            </h2>

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

              <div className="forgot-password">
                <span onClick={handleForgotPassword}>
                  Forgot Password?
                </span>
              </div>

              <button
                type="submit"
                className="login-button"
              >
                Start Your Journey <span>→</span>
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

      {/* Forgot Password Modal */}

      {showForgotPassword && (
        <div className="forgot-modal-overlay">

          <div className="forgot-modal">

            <button
              className="forgot-close"
              onClick={closeForgotPassword}
            >
              ×
            </button>

            <div className="forgot-modal-icon">
              🔐
            </div>

            {forgotStep === 1 && (
              <>
                <h2>Forgot Password?</h2>

                <p>
                  Enter your registered email address.
                  We'll send you a 6-digit OTP.
                </p>

                <form onSubmit={handleSendOtp}>

                  <label>Email Address</label>

                  <input
                    type="email"
                    placeholder="you@example.com"
                    value={forgotEmail}
                    onChange={(e) =>
                      setForgotEmail(e.target.value)
                    }
                    required
                  />

                  <button
                    type="submit"
                    className="login-button"
                    disabled={loading}
                  >
                    {loading ? 'Sending...' : 'Send OTP'}
                  </button>

                </form>
              </>
            )}

            {forgotStep === 2 && (
              <>
                <h2>Verify OTP</h2>

                <p>
                  Enter the 6-digit OTP sent to:
                  <br />
                  <strong>{forgotEmail}</strong>
                </p>

                <form onSubmit={handleVerifyOtp}>

                  <label>OTP</label>

                  <input
                    type="text"
                    placeholder="Enter 6-digit OTP"
                    value={otp}
                    onChange={(e) =>
                      setOtp(
                        e.target.value
                          .replace(/\D/g, '')
                          .slice(0, 6)
                      )
                    }
                    maxLength="6"
                    required
                  />

                  <button
                    type="submit"
                    className="login-button"
                    disabled={loading}
                  >
                    {loading ? 'Verifying...' : 'Verify OTP'}
                  </button>

                </form>

                <button
                  className="back-link"
                  onClick={() => {
                    setForgotStep(1)
                    setForgotMessage('')
                  }}
                >
                  ← Change Email
                </button>
              </>
            )}

            {forgotStep === 3 && (
              <>
                <h2>Set New Password</h2>

                <p>
                  Create a new password for your SmartTrip
                  account.
                </p>

                <form onSubmit={handleResetPassword}>

                  <label>New Password</label>

                  <input
                    type="password"
                    placeholder="Minimum 6 characters"
                    value={newPassword}
                    onChange={(e) =>
                      setNewPassword(e.target.value)
                    }
                    minLength="6"
                    required
                  />

                  <label>Confirm Password</label>

                  <input
                    type="password"
                    placeholder="Re-enter your password"
                    value={confirmPassword}
                    onChange={(e) =>
                      setConfirmPassword(e.target.value)
                    }
                    minLength="6"
                    required
                  />

                  <button
                    type="submit"
                    className="login-button"
                    disabled={loading}
                  >
                    {loading
                      ? 'Resetting...'
                      : 'Reset Password'}
                  </button>

                </form>
              </>
            )}

            {forgotMessage && (
              <p className="forgot-message">
                {forgotMessage}
              </p>
            )}

          </div>
        </div>
      )}

    </div>
  )
}

export default Login