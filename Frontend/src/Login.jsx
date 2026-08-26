import { useState } from 'react'

function Login({ onRegister }) {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [message, setMessage] = useState('')

  const handleLogin = async (e) => {
    e.preventDefault()
    setMessage('Logging in...')

    try {
      const response = await fetch('http://localhost:8080/api/users/login', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          email: email,
          password: password,
        }),
      })

      if (response.ok) {
        const user = await response.json()

        console.log('Logged in user:', user)

        setMessage('Login successful! Welcome back.')

        setEmail('')
        setPassword('')
      } else {
        const errorText = await response.text()

        console.error('Login failed:', errorText)

        setMessage('Invalid email or password.')
      }
    } catch (error) {
      console.error('LOGIN ERROR:', error)

      setMessage(`Backend connection failed: ${error.message}`)
    }
  }

  return (
    <div className="login-page">
      <div className="login-card">
        <h1>Welcome Back</h1>

        <p>Login to continue your trip planning</p>

        <form onSubmit={handleLogin}>
          <label>Email</label>

          <input
            type="email"
            placeholder="Enter your email"
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

          <button type="submit">
            Login
          </button>
        </form>

        {message && (
          <p className="register-text">
            {message}
          </p>
        )}

        <p className="register-text">
          Don't have an account?{' '}
          <span onClick={onRegister}>
            Register
          </span>
        </p>
      </div>
    </div>
  )
}

export default Login