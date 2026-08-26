function Login({ onRegister }) {
  return (
    <div className="login-page">
      <div className="login-card">
        <h1>Welcome Back</h1>

        <p>Login to continue your trip planning</p>

        <form>
          <label>Email</label>

          <input
            type="email"
            placeholder="Enter your email"
          />

          <label>Password</label>

          <input
            type="password"
            placeholder="Enter your password"
          />

          <button type="submit">
            Login
          </button>
        </form>

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