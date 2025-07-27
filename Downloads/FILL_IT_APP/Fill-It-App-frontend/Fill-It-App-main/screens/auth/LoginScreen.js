import React, { useState } from 'react';
import {
  View,
  Text,
  TextInput,
  TouchableOpacity,
  StyleSheet,
  Image,
  SafeAreaView,
  KeyboardAvoidingView,
  Platform,
} from 'react-native';
import Icon from 'react-native-vector-icons/MaterialIcons';
import MaterialCommunityIcons from 'react-native-vector-icons/MaterialCommunityIcons';
import { widthPercentageToDP as wp, heightPercentageToDP as hp } from 'react-native-responsive-screen';
import { RFValue } from 'react-native-responsive-fontsize';

export default function LoginScreen({ navigation }) {
  const [emailOrPhone, setEmailOrPhone] = useState('');
  const [password, setPassword] = useState('');

  const handleLogin = () => {
    if (emailOrPhone.trim() !== '' && password.trim() !== '') {
      navigation.navigate('CustomerFlow');
    } else {
      console.log('Please fill in both email/phone and password.');
    }
  };

  return (
    <SafeAreaView style={styles.container}>
      <KeyboardAvoidingView
        behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
        style={styles.keyboardAvoidingContainer}
      >
        <Image source={require('../../assets/logo.png')} style={styles.logo} />

        <View style={styles.illustrationWrapper}>
          <Image source={require('../../assets/illustration.png')} style={styles.illustration} />
        </View>

        <Text style={styles.title}>Sign in</Text>

        <View style={styles.inputContainer}>
          <Icon name="email" size={RFValue(20)} color="#fff" style={styles.icon} />
          <TextInput
            placeholder="Email or Phone Number"
            style={styles.input}
            placeholderTextColor="#ccc"
            value={emailOrPhone}
            onChangeText={setEmailOrPhone}
            keyboardType="email-address"
            autoCapitalize="none"
          />
        </View>

        <View style={styles.inputContainer}>
          <Icon name="lock" size={RFValue(20)} color="#fff" style={styles.icon} />
          <TextInput
            placeholder="Password"
            secureTextEntry
            style={styles.input}
            placeholderTextColor="#ccc"
            value={password}
            onChangeText={setPassword}
          />
        </View>

        <TouchableOpacity
          style={styles.button}
          onPress={handleLogin}
        >
          <Text style={styles.buttonText}>LOGIN</Text>
        </TouchableOpacity>

        <Text style={styles.orText}>or login with</Text>

        <View style={styles.socialContainer}>
          <TouchableOpacity style={styles.socialButton}>
            <Image source={require('../../assets/google-icon.png')} style={{ width: wp('6%'), height: wp('6%') }} />
          </TouchableOpacity>
          <TouchableOpacity style={styles.socialButton}>
            <MaterialCommunityIcons name="apple" size={wp('6%')} color="#000" />
          </TouchableOpacity>
        </View>

        <TouchableOpacity onPress={() => navigation.navigate('RoleSelection')}>
          <Text style={styles.link}>
            Don't have an account? Sign Up
          </Text>
        </TouchableOpacity>
      </KeyboardAvoidingView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#0F1A24',
    paddingHorizontal: wp('7%'),
    alignItems: 'center',
  },
  keyboardAvoidingContainer: {
    flex: 1,
    width: '100%',
    alignItems: 'center',
    justifyContent: 'center',
  },
 logo: {
    width: wp('22%'),
    height: hp('4%'),
    resizeMode: 'contain',
    position: 'absolute',
    top: hp('6%'),
    left: wp('3%'),
  },
  illustrationWrapper: {
    marginTop: hp('0%'),
    alignItems: 'center',
    justifyContent: 'center',
    shadowColor: '#FF5733',
    shadowOffset: { width: 0, height: 0 },
    shadowOpacity: 0.5,
    shadowRadius: 10,
    elevation: 10,
  },
  illustration: {
    width: wp('70%'),
    height: hp('30%'),
    resizeMode: 'contain',
  },
  title: {
    fontSize: RFValue(24),
    color: '#ffffff',
    marginTop: hp('2%'),
    marginBottom: hp('2%'),
    fontWeight: '600',
  },
  inputContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    width: '100%',
    backgroundColor: '#1c2a3a',
    borderRadius: wp('2.5%'),
    marginBottom: hp('1.5%'),
    paddingHorizontal: wp('3%'),
  },
  icon: {
    marginRight: wp('2%'),
  },
  input: {
    flex: 1,
    paddingVertical: hp('2%'),
    color: '#fff',
  },
  button: {
    width: '100%',
    backgroundColor: '#FF5733',
    padding: hp('2%'),
    borderRadius: wp('2.5%'),
    marginBottom: hp('3%'),
    alignItems: 'center',
  },
  buttonText: {
    color: '#fff',
    fontWeight: 'bold',
    fontSize: RFValue(14),
  },
  orText: {
    color: '#ccc',
    marginBottom: hp('0.5%'),
  },
  socialContainer: {
    flexDirection: 'row',
    justifyContent: 'center',
    width: wp('40%'),
    marginTop: hp('0.5%'),
    marginBottom: hp('2%'),
  },
  socialButton: {
    backgroundColor: '#fff',
    padding: hp('1%'),
    borderRadius: wp('2.5%'),
    alignItems: 'center',
    width: wp('12%'),
    marginHorizontal: wp('2%'),
  },
  link: {
    color: '#ccc',
    marginTop: hp('1.5%'),
    marginBottom: hp('1.5%'),
    fontSize: RFValue(12),
  },
});
