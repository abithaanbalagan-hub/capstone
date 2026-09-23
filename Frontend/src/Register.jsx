import { useState } from 'react'

function Register({ onLogin }) {
  const [name, setName] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')

  const [otp, setOtp] = useState('')
  const [otpMode, setOtpMode] = useState(false)

  const [message, setMessage] = useState('')
  const [loading, setLoading] = useState(false)

  const handleRegister = async (e) => {
    e.preventDefault()

    setMessage('')
    setLoading(true)

    try {
      const response = await fetch(
        'http://localhost:8080/api/users',
        {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({
            name: name,
            email: email,
            password: password,
          }),
        }
      )

      const responseText = await response.text()

      if (response.ok) {
        setOtpMode(true)

        setMessage(
          'OTP sent to your email. Please enter the OTP below.'
        )
      } else {
        console.error(
          'Backend response:',
          responseText
        )

        setMessage(
          responseText ||
          `Registration failed. Server returned ${response.status}`
        )
      }
    } catch (error) {
      console.error(
        'REGISTER ERROR:',
        error
      )

      setMessage(
        `Backend connection failed: ${error.message}`
      )
    } finally {
      setLoading(false)
    }
  }

  const handleVerifyOtp = async (e) => {
    e.preventDefault()

    setMessage('')
    setLoading(true)

    try {
      const response = await fetch(
        'http://localhost:8080/api/users/verify-registration-otp',
        {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({
            email: email,
            otp: otp,
          }),
        }
      )

      const responseText = await response.text()

      if (response.ok) {
        setMessage(
          'Registration successful! Welcome to SmartTrip Planner. You can login now.'
        )

        setName('')
        setEmail('')
        setPassword('')
        setOtp('')
        setOtpMode(false)
      } else {
        console.error(
          'OTP verification response:',
          responseText
        )

        setMessage(
          responseText ||
          'Invalid or expired OTP.'
        )
      }
    } catch (error) {
      console.error(
        'OTP VERIFICATION ERROR:',
        error
      )

      setMessage(
        `Backend connection failed: ${error.message}`
      )
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="login-page">
      <div className="login-card">

        {!otpMode ? (
          <>
            <h1>Create Account</h1>

            <p>
              Create your account to start planning your trips
            </p>

            <form onSubmit={handleRegister}>

              <label>Name</label>

              <input
                type="text"
                placeholder="Enter your name"
                value={name}
                onChange={(e) =>
                  setName(e.target.value)
                }
                required
              />

              <label>Email</label>

              <input
                type="email"
                placeholder="Enter your email"
                value={email}
                onChange={(e) =>
                  setEmail(e.target.value)
                }
                required
              />

              <label>Password</label>

              <input
                type="password"
                placeholder="Create a password"
                value={password}
                onChange={(e) =>
                  setPassword(e.target.value)
                }
                minLength="6"
                required
              />

              <button
                type="submit"
                disabled={loading}
              >
                {loading
                  ? 'Sending OTP...'
                  : 'Register'}
              </button>

            </form>
          </>
        ) : (
          <>
            <h1>Verify Email</h1>

            <p>
              We have sent a 6-digit OTP to
              <br />
              <strong>{email}</strong>
            </p>

            <form onSubmit={handleVerifyOtp}>

              <label>Enter OTP</label>

              <input
                type="text"
                placeholder="Enter 6-digit OTP"
                value={otp}
                onChange={(e) =>
                  setOtp(e.target.value)
                }
                maxLength="6"
                inputMode="numeric"
                required
              />

              <button
                type="submit"
                disabled={loading}
              >
                {loading
                  ? 'Verifying...'
                  : 'Verify OTP'}
              </button>

            </form>

            <p
              className="register-text"
              style={{
                marginTop: '15px',
                cursor: 'pointer'
              }}
              onClick={() => {
                setOtpMode(false)
                setOtp('')
                setMessage('')
              }}
            >
              ← Back to Registration
            </p>
          </>
        )}

        {message && (
          <p className="register-text">
            {message}
          </p>
        )}

        <p className="register-text">
          Already have an account?{' '}
          <span onClick={onLogin}>
            Login
          </span>
        </p>

      </div>
    </div>
  )
}

export default Register