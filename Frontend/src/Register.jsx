import { useState } from 'react'

function Register({ onLogin }) {
  const [name, setName] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [message, setMessage] = useState('')

  const handleRegister = async (e) => {
    e.preventDefault()
    setMessage('Registering...')

    try {
      const response = await fetch('http://localhost:8080/api/users', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          name: name,
          email: email,
          password: password,
        }),
      })

      if (response.ok) {
        setMessage('Registration successful! You can login now.')

        setName('')
        setEmail('')
        setPassword('')
      } else {
        const errorText = await response.text()

        console.error('Backend response:', errorText)

        setMessage(
          `Registration failed. Server returned ${response.status}`
        )
      }
    } catch (error) {
      console.error('REGISTER ERROR:', error)

      setMessage(
        `Backend connection failed: ${error.message}`
      )
    }
  }

  return (
    <div className="login-page">
      <div className="login-card">
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
            onChange={(e) => setName(e.target.value)}
            required
          />

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
            placeholder="Create a password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />

          <button type="submit">
            Register
          </button>
        </form>

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