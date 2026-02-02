// Test simple de l'API d'enregistrement
const testRegister = async () => {
  try {
    const response = await fetch('http://localhost:8080/api/auth/register', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        username: 'testuser2',
        email: 'test2@example.com',
        password: 'password123',
        role: 'STUDENT'
      })
    });

    console.log('Status:', response.status);
    console.log('Headers:', Object.fromEntries(response.headers.entries()));
    
    if (response.ok) {
      const data = await response.json();
      console.log('Success:', data);
    } else {
      const error = await response.text();
      console.log('Error:', error);
    }
  } catch (error) {
    console.error('Network error:', error);
  }
};

testRegister();