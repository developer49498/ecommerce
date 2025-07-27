import React, { useState, useEffect } from 'react';
import {
  View,
  Text,
  TextInput,
  TouchableOpacity,
  StyleSheet,
  SafeAreaView,
  Platform,
  Image,
  PermissionsAndroid,
  ActivityIndicator,
} from 'react-native';
import MapView, { Marker, Polyline } from 'react-native-maps';
import Geolocation from '@react-native-community/geolocation';
import {
  widthPercentageToDP as wp,
  heightPercentageToDP as hp,
} from 'react-native-responsive-screen';

import DatePicker from 'react-native-date-picker';

import Icon from 'react-native-vector-icons/MaterialIcons';
import { useSafeAreaInsets } from 'react-native-safe-area-context';

const darkMapStyle = [
  { elementType: 'geometry', stylers: [{ color: '#212121' }] },
  { elementType: 'labels.icon', stylers: [{ visibility: 'off' }] },
  { elementType: 'labels.text.fill', stylers: [{ color: '#757575' }] },
  { elementType: 'labels.text.stroke', stylers: [{ color: '#212121' }] },
  {
    featureType: 'administrative',
    elementType: 'geometry',
    stylers: [{ color: '#757575' }],
  },
  {
    featureType: 'landscape',
    elementType: 'geometry',
    stylers: [{ color: '#1b1b1b' }],
  },
  {
    featureType: 'poi',
    elementType: 'geometry',
    stylers: [{ color: '#2c2c2c' }],
  },
  {
    featureType: 'road',
    elementType: 'geometry.fill',
    stylers: [{ color: '#2c2c2c' }],
  },
  {
    featureType: 'road',
    elementType: 'geometry.stroke',
    stylers: [{ color: '#212121' }],
  },
  {
    featureType: 'transit',
    elementType: 'geometry',
    stylers: [{ color: '#2f3948' }],
  },
  {
    featureType: 'water',
    elementType: 'geometry',
    stylers: [{ color: '#000000' }],
  },
];

export default function HomeScreen({ navigation }) {
  const [from, setFrom] = useState('');
  const [to, setTo] = useState('');
  const [date, setDate] = useState(new Date());
  const [showDatePicker, setShowDatePicker] = useState(false);
  const [activeTab, setActiveTab] = useState('Home');
  const [region, setRegion] = useState(null);
  const [routeCoordinates, setRouteCoordinates] = useState([]);
  const [isLoadingRoute, setIsLoadingRoute] = useState(false);
  const [routeError, setRouteError] = useState(null);

  const insets = useSafeAreaInsets();

  const COLORS = {
    background: '#1A212E',
    card: '#273244',
    primary: '#1ABC9C',
    accent: '#FF7518',
    textPrimary: '#ECF0F1',
    textSecondary: '#95A5A6',
    searchButtonOrange: '#FFAC1C',
    bottomNavBackground: '#0F1A24',
    divider: '#444',
  };

  useEffect(() => {
    const requestLocationPermission = async () => {
      try {
        const granted = await PermissionsAndroid.request(
          PermissionsAndroid.PERMISSIONS.ACCESS_FINE_LOCATION
        );
        if (granted === PermissionsAndroid.RESULTS.GRANTED) {
          Geolocation.getCurrentPosition(
            position => {
              const { latitude, longitude } = position.coords;
              setRegion({
                latitude,
                longitude,
                latitudeDelta: 0.01,
                longitudeDelta: 0.01,
              });
              setRouteError(null);
            },
            error => {
              console.log(error);
              setRouteError('Could not get current location. Please enable location services.');
            },
            { enableHighAccuracy: true, timeout: 15000, maximumAge: 10000 }
          );
        } else {
          console.log('Location permission denied');
          setRouteError('Location permission denied. Map features may be limited.');
        }
      } catch (err) {
        console.warn(err);
        setRouteError('Error requesting location permission.');
      }
    };
    requestLocationPermission();
  }, []);


  const decodePolyline = (encoded) => {
    let points = [];
    let index = 0,
      len = encoded.length;
    let lat = 0,
      lng = 0;

    while (index < len) {
      let b,
        shift = 0,
        result = 0;
      do {
        b = encoded.charCodeAt(index++) - 63;
        result |= (b & 0x1f) << shift;
        shift += 5;
      } while (b >= 0x20);
      let dlat = ((result & 1) ? ~(result >> 1) : (result >> 1));
      lat += dlat;

      shift = 0;
      result = 0;
      do {
        b = encoded.charCodeAt(index++) - 63;
        result |= (b & 0x1f) << shift;
        shift += 5;
      } while (b >= 0x20);
      let dlng = ((result & 1) ? ~(result >> 1) : (result >> 1));
      lng += dlng;

      points.push({ latitude: (lat / 1e5), longitude: (lng / 1e5) });
    }
    return points;
  };

  const fetchRoute = async () => {
    if (!from || !to) {
      setRouteError('Please enter both "From" and "To" locations to search for a route.');
      return;
    }

    setIsLoadingRoute(true);
    setRouteError(null);
    setRouteCoordinates([]);
 
    const apiKey = 'AIzaSyB4Hvzd1wZpWGPdHAaG6VTb7G6a7WVkz24';
    const directionsUrl = `https://maps.googleapis.com/maps/api/directions/json?origin=${encodeURIComponent(from)}&destination=${encodeURIComponent(to)}&key=${apiKey}`;

    try {
      const response = await fetch(directionsUrl);
      const data = await response.json();
      console.log('Google Directions API Response:', data);

      if (data.routes && data.routes.length > 0) {
        const points = data.routes[0].overview_polyline.points;
        const decodedPoints = decodePolyline(points);
        setRouteCoordinates(decodedPoints);

        if (decodedPoints.length > 0) {
          const minLat = Math.min(...decodedPoints.map(p => p.latitude));
          const maxLat = Math.max(...decodedPoints.map(p => p.latitude));
          const minLng = Math.min(...decodedPoints.map(p => p.longitude));
          const maxLng = Math.max(...decodedPoints.map(p => p.longitude));

          setRegion({
            latitude: (minLat + maxLat) / 2,
            longitude: (minLng + maxLng) / 2,
            latitudeDelta: (maxLat - minLat) * 1.2,
            longitudeDelta: (maxLng - minLng) * 1.2,
          });
        }
      } else {
        if (data.status) {
          setRouteError(`No route found: ${data.status}. Please try again.`);
        } else {
          setRouteError('No route found for the given locations. Please try again.');
        }
      }
    } catch (error) {
      console.error('Error fetching route:', error);
      setRouteError('Failed to fetch route. Please check your internet connection or API key.');
    } finally {
      setIsLoadingRoute(false);
    }
  };

  const handleTabPress = (tabName) => {
    setActiveTab(tabName);
    navigation.navigate(tabName);
  };

  const styles = createStyles(COLORS, insets);

  return (
    <SafeAreaView style={styles.container}>
      <View style={StyleSheet.absoluteFillObject}>
        {region ? (
          <MapView
            style={StyleSheet.absoluteFill}
            region={region}
            showsUserLocation={true}
            showsTraffic={true}
            customMapStyle={darkMapStyle}
          >
            {!routeCoordinates.length > 0 && <Marker coordinate={region} title="You are here" />}

            {routeCoordinates.length > 0 && (
              <>
                <Polyline
                  coordinates={routeCoordinates}
                  strokeWidth={4}
                  strokeColor={COLORS.accent}
                />
                <Marker
                  coordinate={routeCoordinates[0]}
                  title="Origin"
                  pinColor="green"
                />
                <Marker
                  coordinate={routeCoordinates[routeCoordinates.length - 1]}
                  title="Destination"
                  pinColor="red"
                />
              </>
            )}
          </MapView>
        ) : (
          <View style={styles.mapMockup}>
            <Text style={styles.mockupText}>Fetching Your Location...</Text>
            {routeError && <Text style={styles.errorText}>{routeError}</Text>}
          </View>
        )}
      </View>

      <View style={styles.card}>
        <View style={styles.inputContainer}>
          <Icon name="location-on" size={hp('2.5%')} color={COLORS.textSecondary} style={styles.icon} />
          <TextInput
            placeholder="From"
            placeholderTextColor={COLORS.textSecondary}
            style={styles.input}
            value={from}
            onChangeText={setFrom}
          />
        </View>
        <View style={styles.inputContainer}>
          <Icon name="location-on" size={hp('2.5%')} color={COLORS.textSecondary} style={styles.icon} />
          <TextInput
            placeholder="To"
            placeholderTextColor={COLORS.textSecondary}
            style={styles.input}
            value={to}
            onChangeText={setTo}
          />
        </View>
        <TouchableOpacity style={styles.datePickerContainer} onPress={() => setShowDatePicker(true)}>
          <Image
            source={require('../../assets/icons/calendar.png')}
            style={[styles.dateIcon, { tintColor: COLORS.textPrimary, width: 20, height: 20 }]}
          />
          <Text style={styles.dateText}>{date.toDateString()}</Text>
        </TouchableOpacity>

        <DatePicker
          modal
          open={showDatePicker}
          date={date}
          mode="date"
          theme="dark"
          onConfirm={(selectedDate) => {
            setShowDatePicker(false);
            setDate(selectedDate);
          }}
          onCancel={() => {
            setShowDatePicker(false);
          }}
        />

        <TouchableOpacity style={styles.searchButton} onPress={fetchRoute} disabled={isLoadingRoute}>
          {isLoadingRoute ? (
            <ActivityIndicator color={COLORS.textPrimary} />
          ) : (
            <Text style={styles.searchButtonText}>Book Now</Text>
          )}
        </TouchableOpacity>
        {routeError && <Text style={styles.errorText}>{routeError}</Text>}
      </View>
    </SafeAreaView>
  );
}

const createStyles = (COLORS, insets) =>
  StyleSheet.create({
    container: {
      flex: 1,
      backgroundColor: COLORS.background,
    },
    mapMockup: {
      flex: 1,
      justifyContent: 'center',
      alignItems: 'center',
      backgroundColor: '#2c2c2c',
    },
    mockupText: {
      color: COLORS.textSecondary,
      fontSize: hp('2.5%'),
    },
    card: {
      position: 'absolute',
      bottom: insets.bottom + 10,
      left: wp('5%'),
      right: wp('5%'),
      backgroundColor: COLORS.card,
      borderRadius: wp('4%'),
      padding: wp('4%'),
      zIndex: 10,
      shadowColor: '#000',
      shadowOffset: {
        width: 0,
        height: 4,
      },
      shadowOpacity: 0.3,
      shadowRadius: 4.65,
      elevation: 8,
    },
    inputContainer: {
      flexDirection: 'row',
      alignItems: 'center',
      backgroundColor: '#334054',
      borderRadius: wp('2.5%'),
      paddingHorizontal: wp('3%'),
      marginBottom: hp('1.5%'),
    },
    input: {
      flex: 1,
      color: COLORS.textPrimary,
      paddingVertical: hp('1.5%'),
      fontSize: hp('1.8%'),
    },
    icon: {
      marginRight: wp('2%'),
    },
    divider: {
      height: 1,
      backgroundColor: COLORS.divider,
      marginVertical: hp('1%'),
    },
    dateContainer: {
      flexDirection: 'row',
      justifyContent: 'space-between',
      alignItems: 'center',
      marginTop: hp('1%'),
    },
    dateText: {
      color: COLORS.textPrimary,
      fontSize: hp('2%'),
    },
    searchButton: {
      backgroundColor: COLORS.searchButtonOrange,
      paddingVertical: hp('1.5%'),
      borderRadius: wp('2.5%'),
      marginTop: hp('1.5%'),
      alignItems: 'center',
    },
    searchButtonText: {
      color: '#fff',
      fontSize: hp('2%'),
      fontWeight: 'bold',
    },
    errorText: {
      color: '#ff6b6b',
      textAlign: 'center',
      marginTop: hp('1%'),
      fontSize: hp('1.8%'),
    },
    datePickerContainer: {
      flexDirection: 'row',
      alignItems: 'center',
      backgroundColor: '#334054',
      borderRadius: wp('2.5%'),
      padding: hp('1.5%'),
      marginBottom: hp('1.5%'),
    },
    dateIcon: {
      marginRight: wp('3%'),
    },
  });