{
  "$schema": "http://json-schema.org/draft-04/schema#",
  "type": "object",
  "properties": {
    "general": {
      "type": "object",
      "properties": {
        "hypervisor": {
          "type": "object",
          "properties": {
            "hypervisor": {
              "type": "string",
              "enum": [
                "KVM",
                "VMWare ESXi"
              ],
              "default": "KVM"
            },
            "drivers": {
              "type": "string",
              "maxLength": 300,
              "pattern": "^[A-Za-z0-9_,-]*$"
            },
            "containerFeaturesDescription": {
              "type": "string",
              "maxLength": 1000,
              "pattern": "^[A-Za-z0-9_, -]*$"
            }
          },
          "additionalProperties": false
        },
        "image": {
          "type": "object",
          "properties": {
            "format": {
              "type": "string",
              "enum": [
                "aki",
                "ami",
                "ari",
                "iso",
                "qcow2",
                "raw",
                "vdi",
                "vhd",
                "vmdk"
              ],
              "default": "qcow2"
            },
            "providedBy": {
              "type": "string",
              "enum": [
                "Vendor"
              ],
              "default": "Vendor"
            },
            "bootDiskSizePerVM": {
              "type": "number",
              "maximum": 100
            },
            "ephemeralDiskSizePerVM": {
              "type": "number",
              "maximum": 400
            }
          },
          "additionalProperties": false
        },
        "recovery": {
          "type": "object",
          "properties": {
            "pointObjective": {
              "type": "number",
              "minimum": 0,
              "exclusiveMinimum": true,
              "maximum": 15,
              "exclusiveMaximum ": true
            },
            "timeObjective": {
              "type": "number",
              "minimum": 0,
              "exclusiveMinimum": true,
              "maximum": 300,
              "exclusiveMaximum ": true
            },
            "vmProcessFailuresHandling": {
              "type": "string"
            }
          },
          "additionalProperties": false
        },
        "dnsConfiguration": {
          "type": "string"
        },
        "vmCloneUsage": {
          "type": "string",
          "maximum": 300
        }
      },
      "additionalProperties": false
    },
    "compute": {
      "type": "object",
      "properties": {
        "vmSizing": {
          "type": "object",
          "properties": {
            "numOfCPUs": {
              "type": "number",
              "minimum": 0,
              "exclusiveMinimum": true,
              "maximum": 16,
              "default": 2
            },
            "fileSystemSizeGB": {
              "type": "number",
              "minimum": 0,
              "exclusiveMinimum": true,
              "default": 5
            },
            "persistentStorageVolumeSize": {
              "type": "number",
              "minimum": 0,
              "exclusiveMinimum": true
            },
            "IOOperationsPerSec": {
              "type": "number",
              "minimum": 0,
              "exclusiveMinimum": true
            }
          },
          "additionalProperties": false
        },
        "numOfVMs": {
          "type": "object",
          "properties": {
            "minimum": {
              "type": "number",
              "minimum": 0,
              "exclusiveMinimum": true,
              "maximum": 100
            },
            "maximum": {
              "type": "number",
            "minimum": <#if (componentQuestionnaireData.compute.numOfVMs.minimum)??
            && (componentQuestionnaireData.compute.numOfVMs.minimum)?is_number
            && ((componentQuestionnaireData.compute.numOfVMs.minimum) > 0
            && (componentQuestionnaireData.compute.numOfVMs.minimum) <= 100)>
            ${componentQuestionnaireData.compute.numOfVMs.minimum}<#else>
            0</#if> ,
              "exclusiveMinimum": true,
              "maximum": 100
            },
            "CpuOverSubscriptionRatio": {
              "type": "string",
              "enum": [
                "1:1",
                "4:1",
                "16:1"
              ],
              "default": "4:1"
            },
            "MemoryRAM": {
              "type": "string",
              "enum": [
                "2 GB",
                "4 GB",
                "8 GB"
              ],
              "default": "2 GB"
            }
          },
          "additionalProperties": false
        },
        "guestOS": {
          "type": "object",
          "properties": {
            "name": {
              "type": "string",
              "maxLength": 50
            },
            "bitSize": {
              "type": "number",
              "enum": [
                64,
                32
              ],
              "default": 64
            },
            "tools": {
              "type": "string"
            }
          },
          "additionalProperties": false
        }
      },
      "additionalProperties": false
    },
    "highAvailabilityAndLoadBalancing": {
      "type": "object",
      "properties": {
        "failureLoadDistribution": {
          "type": "string",
          "maxLength": 1000
        },
        "nkModelImplementation": {
          "type": "string",
          "maxLength": 1000
        },
        "architectureChoice": {
          "type": "string",
          "maxLength": 1000
        },
        "slaRequirements": {
          "type": "string",
          "maxLength": 1000
        },
        "horizontalScaling": {
          "type": "string",
          "maxLength": 1000
        },
        "loadDistributionMechanism": {
          "type": "string",
          "maxLength": 1000
        }
      },
      "additionalProperties": false
    },
    "network": {
      "type": "object",
      "properties": {
        "networkCapacity": {
          "type": "object",
          "properties": {
            "protocolWithHighestTrafficProfileAcrossAllNICs": {
              "type": "string",
              "enum": [
                "",
                "TCP",
                "UDP",
                "SCTP",
                "IPsec"
              ],
              "default": ""
            },
            "networkTransactionsPerSecond": {
              "type": "number"
            }
          },
          "additionalProperties": false
        }
      },
      "additionalProperties": false
    },
    "storage": {
      "type": "object",
      "properties": {
        "backup": {
          "type": "object",
          "properties": {
            "backupType": {
              "type": "string",
              "enum": [
                "On Site",
                "Off Site"
              ],
              "default": "On Site"
            },
            "backupStorageSize": {
              "type": "number"
            },
            "backupSolution": {
              "type": "string"
            },
            "backupNIC": {
              "type": "string",
              "enum": [
                "", <#if nicNames??><#list nicNames as nicName>
                "${nicName}"<#sep>,</#list></#if>
              ],
              "default": ""
            }
          },
          "additionalProperties": false
        },
        "snapshotBackup": {
          "type": "object",
          "properties": {
            "snapshotFrequency": {
              "type": "number",
              "default": 24,
              "minimum": 1,
              "exclusiveMinimum": true
            }
          },
          "additionalProperties": false
        },
        "logBackup": {
          "type": "object",
          "properties": {
            "sizeOfLogFiles": {
              "type": "number",
              "maximum": 5,
              "exclusiveMaximum": true
            },
            "logBackupFrequency": {
              "type": "number",
              "maximum": 4,
              "exclusiveMaximum": true
            },
            "logRetentionPeriod": {
              "type": "number",
              "maximum": 15,
              "exclusiveMaximum": true
            },
            "logFileLocation": {
              "type": "string",
              "maxLength": 300
            }
          },
          "additionalProperties": false
        }
      },
      "additionalProperties": false
    }
  },
  "additionalProperties": false
}