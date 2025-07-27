import React, { useState } from 'react';
import {
  View,
  Text,
  TouchableOpacity,
  StyleSheet,
  SafeAreaView,
  Platform,
  Image,
  Linking,
  TextInput,
} from 'react-native';

import Icon from 'react-native-vector-icons/MaterialIcons';
import MaterialCommunityIcons from 'react-native-vector-icons/MaterialCommunityIcons';
import {
  widthPercentageToDP as wp,
  heightPercentageToDP as hp,
} from 'react-native-responsive-screen';

import { useSafeAreaInsets } from 'react-native-safe-area-context';

export default function AccountScreen({ navigation }) {
  const insets = useSafeAreaInsets();
  const [activeTab, setActiveTab] = useState('Account');
  const [showPersonalInfoModal, setShowPersonalInfoModal] = useState(false);
  const [editingPhoneNumber, setEditingPhoneNumber] = useState(false);
  const [currentPhoneNumber, setCurrentPhoneNumber] = useState('123-456-7890');

  const [profileData, setProfileData] = useState({
    avatar: require('../../assets/profile.png'),
    name: 'Kaushik Sahoo',
    email: 'sahookaushik1797@email.com',
    phoneNumber: '123-456-7890',
  });


  const COLORS = {
    background: '#0F1A24',
    cardBackground: '#1c2a3a',
    textPrimary: '#ECF0F1',
    textSecondary: '#95A5A6',
    iconColor: '#ECF0F1',
    divider: '#3E4A5C',
    accent: '#FF7518',
    redAccent: '#FF445A',
    modalRowBackground: '#273244',
  };

  const styles = createStyles(COLORS, insets);

  const settingsOptions = [
    { id: 'personal', name: 'Personal Information', icon: 'person-outline' },
    { id: 'notifications', name: 'Notifications', icon: 'notifications-none' },
  ];

  const supportOptions = [
    { id: 'help', name: 'Help Center', icon: 'mail-outline' },
    { id: 'contact', name: 'Contact Us', icon: 'chat' },
  ];

  const handleMenuItemPress = async (id) => {
    console.log(`Pressed: ${id}`);
    if (id === 'contact') {
      const phoneNumber = '918457899093';
      const whatsappUrl = `whatsapp://send?phone=${phoneNumber}`;
      try {
        const supported = await Linking.canOpenURL(whatsappUrl);
        if (supported) {
          await Linking.openURL(whatsappUrl);
        } else {
          alert('WhatsApp is not installed on your device or cannot be opened directly.');
        }
      } catch (error) {
        console.error('Error opening WhatsApp:', error);
        alert('Could not open WhatsApp. Please try again.');
      }
    } else if (id === 'help') {
      const emailAddress = 'cloudnexus@googlegroups.com';
      const mailtoUrl = `mailto:${emailAddress}`;
      try {
        const supported = await Linking.canOpenURL(mailtoUrl);
        if (supported) {
          await Linking.openURL(mailtoUrl);
        } else {
          alert('No email client is installed or configured on your device.');
        }
      } catch (error) {
        console.error('Error opening email client:', error);
        alert('Could not open email client. Please try again.');
      }
    } else if (id === 'personal') {
      setShowPersonalInfoModal(true);
      setCurrentPhoneNumber(profileData.phoneNumber);
      setEditingPhoneNumber(false);
    }
  };

  const handleTabPress = (tabName) => {
    setActiveTab(tabName);
    if (tabName === 'Home') {
      navigation.navigate('Home');
    } else if (tabName === 'History') {
      navigation.navigate('History');
    } else if (tabName === 'Account') {
      navigation.navigate('Account');
    }
  };

  const handleSavePhoneNumber = () => {
    setProfileData(prev => ({ ...prev, phoneNumber: currentPhoneNumber }));
    setEditingPhoneNumber(false);
    alert('Phone number updated!');
  };

  const handleEditAvatar = () => {
    alert('Image picker integration goes here to change avatar!');
  };

  const handleLogout = () => {
    alert('Logged out successfully!');
    navigation.reset({
      index: 0,
      routes: [{ name: 'Login' }],
    });
  };


  return (
    <SafeAreaView style={styles.container}>

      <View style={styles.profileHeader}>
        <Image source={profileData.avatar} style={styles.avatar} />
        <Text style={styles.profileName}>{profileData.name}</Text>
        <Text style={styles.profileEmail}>{profileData.email}</Text>
      </View>

      <View style={styles.sectionContainer}>
        <Text style={styles.sectionTitle}>Settings</Text>
        {settingsOptions.map((item, index) => (
          <TouchableOpacity
            key={item.id}
            style={[
              styles.menuItem,
              index === settingsOptions.length - 1 && { borderBottomWidth: 0 },
            ]}
            onPress={() => handleMenuItemPress(item.id)}
          >
            <Icon name={item.icon} size={24} color={COLORS.iconColor} style={styles.menuIcon} />
            <Text style={styles.menuText}>{item.name}</Text>
            <Icon name="chevron-right" size={24} color={COLORS.iconColor} />
          </TouchableOpacity>
        ))}
      </View>

      <View style={styles.sectionContainer}>
        <Text style={styles.sectionTitle}>Support</Text>
        {supportOptions.map((item, index) => (
          <TouchableOpacity
            key={item.id}
            style={[
              styles.menuItem,
              index === supportOptions.length - 1 && { borderBottomWidth: 0 },
            ]}
            onPress={() => handleMenuItemPress(item.id)}
          >
            <Icon name={item.icon} size={24} color={COLORS.iconColor} style={styles.menuIcon} />
            <Text style={styles.menuText}>{item.name}</Text>
            <Icon name="chevron-right" size={24} color={COLORS.iconColor} />
          </TouchableOpacity>
        ))}
      </View>

      {/* Logout Button - Adjusted width and padding */}
      <TouchableOpacity style={styles.logoutButton} onPress={handleLogout}>
        <Image
          source={require('../../assets/icons/logout.png')}
          style={styles.logoutImageIcon}
        />
        <Text style={styles.logoutButtonText}>Sign Out</Text>
      </TouchableOpacity>

      {showPersonalInfoModal && (
        <View style={styles.modalOverlay}>
          <View style={styles.personalInfoCard}>
            <TouchableOpacity style={styles.modalCloseButton} onPress={() => setShowPersonalInfoModal(false)}>
              <Icon name="close" size={24} color={COLORS.textPrimary} />
            </TouchableOpacity>

            <View style={styles.modalAvatarContainer}>
              <Image source={profileData.avatar} style={styles.modalAvatar} />
              <TouchableOpacity style={styles.editAvatarButton} onPress={handleEditAvatar}>
                <Icon name="edit" size={20} color={COLORS.iconColor} />
              </TouchableOpacity>
            </View>

            <Text style={styles.modalName}>{profileData.name}</Text>
            <Text style={styles.modalEmail}>{profileData.email}</Text>

            <View style={styles.modalRow}>
              <MaterialCommunityIcons name="phone" size={24} color={COLORS.textSecondary} style={styles.modalRowIcon} />
              {editingPhoneNumber ? (
                <TextInput
                  style={styles.modalTextInput}
                  value={currentPhoneNumber}
                  onChangeText={setCurrentPhoneNumber}
                  keyboardType="phone-pad"
                  placeholder="Enter phone number"
                  placeholderTextColor={COLORS.textSecondary}
                />
              ) : (
                <Text style={styles.modalRowText}>{profileData.phoneNumber}</Text>
              )}
              <TouchableOpacity
                style={styles.modalEditSaveButton}
                onPress={() => {
                  if (editingPhoneNumber) {
                    handleSavePhoneNumber();
                  } else {
                    setEditingPhoneNumber(true);
                  }
                }}
              >
                <Icon name={editingPhoneNumber ? "check" : "edit"} size={20} color={COLORS.iconColor} />
              </TouchableOpacity>
            </View>
          </View>
        </View>
      )}
    </SafeAreaView>
  );
}

const createStyles = (COLORS, insets) =>
  StyleSheet.create({
    container: {
      flex: 1,
      backgroundColor: COLORS.background,
      paddingTop: Platform.OS === 'android' ? hp('5%') : insets.top,
      paddingHorizontal: wp('5%'),
    },
    profileHeader: {
      alignItems: 'center',
      paddingVertical: hp('2%'),
      marginBottom: hp('2%'),
    },
    avatar: {
      width: wp('30%'),
      height: wp('30%'),
      borderRadius: wp('15%'),
      marginBottom: hp('1.5%'),
      backgroundColor: COLORS.cardBackground,
    },
    profileName: {
      fontSize: wp('6%'),
      fontWeight: 'bold',
      color: COLORS.textPrimary,
      marginBottom: hp('0.5%'),
    },
    profileEmail: {
      fontSize: wp('4%'),
      color: COLORS.textSecondary,
    },
    sectionContainer: {
      backgroundColor: COLORS.cardBackground,
      borderRadius: wp('3%'),
      marginBottom: hp('2.5%'),
      paddingHorizontal: wp('4%'),
    },
    sectionTitle: {
      fontSize: wp('4.5%'),
      fontWeight: '600',
      color: COLORS.textPrimary,
      paddingVertical: hp('2%'),
    },
    menuItem: {
      flexDirection: 'row',
      alignItems: 'center',
      paddingVertical: hp('2%'),
      borderBottomWidth: 1,
      borderBottomColor: COLORS.divider,
    },
    menuIcon: {
      marginRight: wp('4%'),
    },
    menuText: {
      flex: 1,
      fontSize: wp('4%'),
      color: COLORS.textPrimary,
    },
    logoutButton: {
      flexDirection: 'row',
      alignItems: 'center',
      justifyContent: 'center',
      backgroundColor: COLORS.redAccent,
      paddingVertical: hp('1.8%'),
      borderRadius: wp('3%'),
      marginTop: hp('2%'),
    },
    logoutImageIcon: {
      width: wp('6%'),
      height: wp('6%'),
      marginRight: wp('2%'),
    },
    logoutButtonText: {
      color: COLORS.textPrimary,
      fontSize: wp('4.5%'),
      fontWeight: 'bold',
    },
    modalOverlay: {
      ...StyleSheet.absoluteFillObject,
      backgroundColor: 'rgba(0,0,0,0.7)',
      justifyContent: 'center',
      alignItems: 'center',
    },
    personalInfoCard: {
      width: wp('90%'),
      backgroundColor: COLORS.cardBackground,
      borderRadius: wp('4%'),
      padding: wp('5%'),
      alignItems: 'center',
    },
    modalCloseButton: {
      position: 'absolute',
      top: hp('1%'),
      right: wp('2%'),
      padding: wp('2%'),
    },
    modalAvatarContainer: {
      marginBottom: hp('2%'),
    },
    modalAvatar: {
      width: wp('25%'),
      height: wp('25%'),
      borderRadius: wp('12.5%'),
      backgroundColor: '#333',
    },
    editAvatarButton: {
      position: 'absolute',
      bottom: 0,
      right: 0,
      backgroundColor: COLORS.accent,
      padding: wp('2%'),
      borderRadius: wp('4%'),
    },
    modalName: {
      fontSize: wp('5.5%'),
      fontWeight: 'bold',
      color: COLORS.textPrimary,
      marginBottom: hp('0.5%'),
    },
    modalEmail: {
      fontSize: wp('4%'),
      color: COLORS.textSecondary,
      marginBottom: hp('3%'),
    },
    modalRow: {
      flexDirection: 'row',
      alignItems: 'center',
      backgroundColor: COLORS.modalRowBackground,
      borderRadius: wp('3%'),
      padding: wp('3%'),
      width: '100%',
    },
    modalRowIcon: {
      marginRight: wp('3%'),
    },
    modalRowText: {
      flex: 1,
      fontSize: wp('4%'),
      color: COLORS.textPrimary,
    },
    modalTextInput: {
      flex: 1,
      fontSize: wp('4%'),
      color: COLORS.textPrimary,
      paddingVertical: 0, // Reset padding
    },
    modalEditSaveButton: {
      padding: wp('1.5%'),
    },
  });
