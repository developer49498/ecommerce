import React from 'react';
import {
  View,
  Text,
  TouchableOpacity,
  StyleSheet,
  Image,
  SafeAreaView,
} from 'react-native';
import {
  widthPercentageToDP as wp,
  heightPercentageToDP as hp,
} from 'react-native-responsive-screen';
import { RFValue } from 'react-native-responsive-fontsize';

export default function RoleSelectionScreen({ navigation }) {
  return (
    <SafeAreaView style={styles.container}>
      <Image source={require('../assets/logo.png')} style={styles.logo} />
      <View style={styles.illustrationWrapper}>
        <Image source={require('../assets/roles.png')} style={styles.roles} />
      </View>
      <Text style={styles.title}>Choose Your Role</Text>
      <TouchableOpacity
        style={styles.button}
        onPress={() => navigation.navigate('Signup', { role: 'Driver' })}
      >
        <Text style={styles.buttonText}>Driver</Text>
      </TouchableOpacity>
      <TouchableOpacity
        style={styles.button}
        onPress={() => navigation.navigate('Signup', { role: 'Customer' })}
      >
        <Text style={styles.buttonText}>Customer</Text>
      </TouchableOpacity>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#0F1A24',
    justifyContent: 'center',
    alignItems: 'center',
    paddingHorizontal: wp('8%'),
  },
  logo: {
    width: wp('22%'),
    height: hp('4%'),
    resizeMode: 'contain',
    position: 'absolute',
    top: hp('5%'),
    left: wp('5%'),
  },
  illustrationWrapper: {
    marginTop: hp('8%'),
    alignItems: 'center',
    justifyContent: 'center',
  },
  roles: {
    width: wp('70%'),
    height: hp('30%'),
    resizeMode: 'contain',
  },
  title: {
    fontSize: RFValue(24),
    color: '#ffffff',
    marginTop: hp('2%'),
    marginBottom: hp('3%'),
    fontWeight: '600',
  },
  button: {
    width: '100%',
    backgroundColor: '#1E90FF',
    padding: hp('2%'),
    borderRadius: 10,
    marginBottom: hp('2.5%'),
    alignItems: 'center',
  },
  buttonText: {
    color: '#fff',
    fontWeight: 'bold',
    fontSize: RFValue(16),
  },
});
