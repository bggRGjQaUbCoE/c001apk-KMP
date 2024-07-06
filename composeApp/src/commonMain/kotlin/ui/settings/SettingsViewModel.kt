package ui.settings

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import logic.repository.UserPreferencesRepo
import util.DeviceUtil.regenerateParams
import util.ViewModelInstance

/**
 * Created by bggRGjQaUbCoE on 2024/5/31
 */
class SettingsViewModel(
    private val userPreferencesRepo: UserPreferencesRepo,
    dispatcher: CoroutineDispatcher,
) : ViewModelInstance(dispatcher) {

    fun setThemeMode(value: Int) {
        viewModelScope.launch(dispatcher) {
            userPreferencesRepo.setThemeMode(value)
        }
    }

    fun setMaterialYou(value: Boolean) {
        viewModelScope.launch(dispatcher) {
            userPreferencesRepo.setMaterialYou(value)
        }
    }

    fun setPureBlack(value: Boolean) {
        viewModelScope.launch(dispatcher) {
            userPreferencesRepo.setPureBlack(value)
        }
    }

    fun setFontScale(value: Float) {
        viewModelScope.launch(dispatcher) {
            userPreferencesRepo.setFontScale(value)
        }
    }

    fun setContentScale(value: Float) {
        viewModelScope.launch(dispatcher) {
            userPreferencesRepo.setContentScale(value)
        }
    }

    fun setSZLMId(value: String) {
        viewModelScope.launch(dispatcher) {
            userPreferencesRepo.setSZLMId(value)
        }
    }

    fun setImageQuality(value: Int) {
        viewModelScope.launch(dispatcher) {
            userPreferencesRepo.setImageQuality(value)
        }
    }

    fun setImageFilter(value: Boolean) {
        viewModelScope.launch(dispatcher) {
            userPreferencesRepo.setImageFilter(value)
        }
    }

    fun setOpenInBrowser(value: Boolean) {
        viewModelScope.launch(dispatcher) {
            userPreferencesRepo.setOpenInBrowser(value)
        }
    }

    fun setShowSquare(value: Boolean) {
        viewModelScope.launch(dispatcher) {
            userPreferencesRepo.setShowSquare(value)
        }
    }

    fun setRecordHistory(value: Boolean) {
        viewModelScope.launch(dispatcher) {
            userPreferencesRepo.setRecordHistory(value)
        }
    }

    fun setShowEmoji(value: Boolean) {
        viewModelScope.launch(dispatcher) {
            userPreferencesRepo.setShowEmoji(value)
        }
    }

    fun setCheckUpdate(value: Boolean) {
        viewModelScope.launch(dispatcher) {
            userPreferencesRepo.setCheckUpdate(value)
        }
    }

    fun setCheckCount(value: Boolean) {
        viewModelScope.launch(dispatcher) {
            userPreferencesRepo.setCheckCount(value)
        }
    }

    fun setVersionName(value: String) {
        viewModelScope.launch(dispatcher) {
            userPreferencesRepo.setVersionName(value)
        }
    }

    fun setApiVersion(value: String) {
        viewModelScope.launch(dispatcher) {
            userPreferencesRepo.setApiVersion(value)
        }
    }

    fun setVersionCode(value: String) {
        viewModelScope.launch(dispatcher) {
            userPreferencesRepo.setVersionCode(value)
        }
    }

    fun setManufacturer(value: String) {
        viewModelScope.launch(dispatcher) {
            userPreferencesRepo.setManufacturer(value)
        }
    }

    fun setBrand(value: String) {
        viewModelScope.launch(dispatcher) {
            userPreferencesRepo.setBrand(value)
        }
    }

    fun setModel(value: String) {
        viewModelScope.launch(dispatcher) {
            userPreferencesRepo.setModel(value)
        }
    }

    fun setBuildNumber(value: String) {
        viewModelScope.launch(dispatcher) {
            userPreferencesRepo.setBuildNumber(value)
        }
    }

    fun setSdkInt(value: String) {
        viewModelScope.launch(dispatcher) {
            userPreferencesRepo.setSdkInt(value)
        }
    }

    fun setAndroidVersion(value: String) {
        viewModelScope.launch(dispatcher) {
            userPreferencesRepo.setAndroidVersion(value)
        }
    }

    fun setUserAgent(value: String) {
        viewModelScope.launch(dispatcher) {
            userPreferencesRepo.setUserAgent(value)
        }
    }

    fun setXAppDevice(value: String) {
        viewModelScope.launch(dispatcher) {
            userPreferencesRepo.setXAppDevice(value)
        }
    }

    fun setXAppToken(value: String) {
        viewModelScope.launch(dispatcher) {
            userPreferencesRepo.setXAppToken(value)
        }
    }

    fun setFollowType(value: Int) {
        viewModelScope.launch(dispatcher) {
            userPreferencesRepo.setFollowType(value)
        }
    }

    fun setThemeType(value: Int) {
        viewModelScope.launch(dispatcher) {
            userPreferencesRepo.setThemeType(value)
        }
    }

    fun setSeedColor(value: String) {
        viewModelScope.launch(dispatcher) {
            userPreferencesRepo.setSeedColor(value)
        }
    }

    fun regenerateParams() {
        viewModelScope.launch(dispatcher) {
            userPreferencesRepo.regenerateParams()
        }
    }

}