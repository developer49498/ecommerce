import React, { useState } from 'react';
import {
  View,
  Text,
  TextInput,
  TouchableOpacity,
  StyleSheet,
  Image,
  SafeAreaView,
  ScrollView,
  Alert,
} from 'react-native';
import {
  widthPercentageToDP as wp,
  heightPercentageToDP as hp,
} from 'react-native-responsive-screen';
import { RFValue } from 'react-native-responsive-fontsize';

export default function SignupScreen({ navigation, route }) {
  const { role } = route.params || {};
  console.log('Role on SignupScreen:', role);
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [phoneNumber, setPhoneNumber] = useState('');
  const [vehicleNumber, setVehicleNumber] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');

  const handleSignup = () => {
    if (!username || !email || !phoneNumber || !password || !confirmPassword) {
      Alert.alert('Error', 'Please fill in all required fields');
      return;
    }

    if (role?.toLowerCase() === 'driver' && !vehicleNumber) {
      Alert.alert('Error', 'Please enter your vehicle number');
      return;
    }

    if (password !== confirmPassword) {
      Alert.alert('Error', 'Passwords do not match');
      return;
    }

    
    console.log({
      role,
      username,
      email,
      phoneNumber,
      vehicleNumber: role?.toLowerCase() === 'driver' ? vehicleNumber : undefined,
      password,
    });

    
    if (role?.toLowerCase() === 'driver') {
      navigation.replace('DriverFlow');
    } else {
      navigation.replace('CustomerFlow');
    }
  };

  const renderIllustration = () => {
    if (role?.toLowerCase() === 'driver') {
      return (
        <Image
          source={require('../../assets/driver-illustration.png')}
          style={styles.illustration}
        />
      );
    } else {
      return (
        <Image
          source={require('../../assets/customer-illustration.png')}
          style={styles.illustration}
        />
      );
    }
  };

  return (
    <SafeAreaView style={styles.container}>
      <ScrollView
        contentContainerStyle={styles.scrollContainer}
        showsVerticalScrollIndicator={false}
      >
        <Image source={require('../../assets/logo.png')} style={styles.logo} />
        {renderIllustration()}
        <Text style={styles.title}>Sign Up</Text>
        {role && <Text style={styles.roleText}>Signing up as: {role}</Text>}

        <TextInput
          placeholder="Username"
          style={styles.input}
          placeholderTextColor="#ccc"
          value={username}
          onChangeText={setUsername}
        />
        <TextInput
          placeholder="Email"
          style={styles.input}
          placeholderTextColor="#ccc"
          value={email}
          onChangeText={setEmail}
        />
        <TextInput
          placeholder="Phone Number"
          style={styles.input}
          placeholderTextColor="#ccc"
          value={phoneNumber}
          onChangeText={setPhoneNumber}
        />
        {role?.toLowerCase() === 'driver' && (
          <TextInput
            placeholder="Vehicle Number"
            style={styles.input}
            placeholderTextColor="#ccc"
            value={vehicleNumber}
            onChangeText={setVehicleNumber}
          />
        )}
        <TextInput
          placeholder="Password"
          secureTextEntry
          style={styles.input}
          placeholderTextColor="#ccc"
          value={password}
          onChangeText={setPassword}
        />
        <TextInput
          placeholder="Confirm Password"
          secureTextEntry
          style={styles.input}
          placeholderTextColor="#ccc"
          value={confirmPassword}
          onChangeText={setConfirmPassword}
        />

        <TouchableOpacity style={styles.button} onPress={handleSignup}>
          <Text style={styles.buttonText}>CREATE ACCOUNT</Text>
        </TouchableOpacity>

        <Text style={styles.link} onPress={() => navigation.navigate('Login')}>
          Already have an account? Sign In
        </Text>
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#0F1A24',
  },
  scrollContainer: {
    alignItems: 'center',
    paddingHorizontal: wp('8%'),
    paddingTop: hp('10%'),
    paddingBottom: hp('5%'),
  },
  logo: {
    width: wp('22%'),
    height: hp('4%'),
    resizeMode: 'contain',
    position: 'absolute',
    top: hp('5%'),
    left: wp('5%'),
  },
  illustration: {
    width: wp('40%'),
    height: hp('13%'),
    resizeMode: 'contain',
    marginBottom: hp('2%'),
  },
  title: {
    fontSize: RFValue(24),
    color: '#ffffff',
    marginBottom: hp('1%'),
    fontWeight: '600',
  },
  roleText: {
    fontSize: RFValue(14),
    color: '#ccc',
    marginBottom: hp('2%'),
  },
  input: {
    width: '100%',
    backgroundColor: '#1c2a3a',
    padding: hp('1.8%'),
    borderRadius: wp('2.5%'),
    marginBottom: hp('2%'),
    color: '#fff',
    fontSize: RFValue(14),
  },
  button: {
    width: '100%',
    backgroundColor: '#5CB85C',
    padding: hp('2%'),
    borderRadius: wp('2.5%'),
    marginBottom: hp('2%'),
    alignItems: 'center',
  },
  buttonText: {
    color: '#fff',
    fontWeight: 'bold',
    fontSize: RFValue(15),
  },
  link: {
    color: '#ccc',
    marginTop: hp('1%'),
    fontSize: RFValue(13),
  },
});
